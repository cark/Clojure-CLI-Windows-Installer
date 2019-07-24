(ns clj-install.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clj-install.logging :as logging]
            [clj-install.dev :as dev]
            [taoensso.timbre :as timbre
             :refer [log  trace  debug  info  warn  error  fatal  report
                     logf tracef debugf infof warnf errorf fatalf reportf
                     spy get-env]]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [expound.alpha :as expound]
            [clojure.set :as set]
            [clj-http.client :as client])
  (:import [java.util.zip ZipInputStream]))

(s/def :install.edn/wrapper-version (s/and string?
                                           #(str/starts-with? % "v")))
(s/def :install.edn/clojure-tools-version string?)
(s/def :install.edn/file (s/keys :req-un [:install.edn/clojure-tools-version :install.edn/wrapper-version]))

(def install-edn-filename "install.edn")

(defn get-install-edn []
  (info "Opening install configuration:" install-edn-filename)
  (if (.exists (io/file install-edn-filename))
    (with-open [s (-> install-edn-filename io/reader java.io.PushbackReader.)]
      (edn/read s))
    (throw (ex-info "Install configuration file was not found."
                    {:filename install-edn-filename
                     :current-directory (System/getProperty "user.dir")}))))

(defn validate-install-edn [edn]
  (if (s/valid? :install.edn/file edn)
    edn
    (throw (ex-info "Invalid configuration file format."
                    {:filename install-edn-filename
                     :expanded-error (expound/expound-str :install.edn/file edn)}))))

(defn get-config []
  (-> (get-install-edn)
      validate-install-edn
      (set/rename-keys {:wrapper-version :config/wrapper-version
                        :clojure-tools-version :config/clojure-tools-version})))

(defn clojure-tools-url [version]
  (str "https://download.clojure.org/install/clojure-tools-" version ".zip"))

(def clojure-tools-dir "ClojureTools")

(defn delete-files-recursively
  [dir]
  (let [dir (io/file dir)]
    (when (.isDirectory dir)
      (doseq [file (.listFiles dir)]
        (delete-files-recursively file)))
    (io/delete-file dir true)))

(defn download-clojure-tools [ctx]
  (info "Deleting" clojure-tools-dir)
  (delete-files-recursively clojure-tools-dir)
  (let [url (clojure-tools-url (:config/clojure-tools-version ctx))]
    (info "Downloading" url)
    (with-open [stream (-> (client/get url {:as :byte-array}) :body io/input-stream ZipInputStream.)]
      (info "Extracting to" clojure-tools-dir)
      (loop []
        (when-let [entry (.getNextEntry stream)]
          (do (if (.isDirectory entry)
                (let [filename (.getName entry)]
                  (info "Directory:" filename)
                  (.mkdir (io/file filename)))
                (let [filename (.getName entry)
                      file (io/file filename)]
                  (info "File:" filename)
                  (io/copy stream file)))
              (recur)))))
    ctx))

(defn -main [& args]
  (try
    (logging/setup-logging)
    (-> (get-config)
        download-clojure-tools)
    (catch Exception ex
      (logging/log-exception ex))))

