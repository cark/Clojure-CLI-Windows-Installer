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
            [clj-http.client :as client]
            [clojure.java.shell :as sh]
            [clj-install.dev :as dev])
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

(defn delete-files-recursively
  [dir]
  (let [dir (io/file dir)]
    (when (.isDirectory dir)
      (doseq [file (.listFiles dir)]
        (delete-files-recursively file)))
    (io/delete-file dir true)))

(defn unzip-to [stream path]
  (let [file (io/file path)]
    (.mkdir file)
    (with-open [stream (ZipInputStream. stream)]
      (info "Extracting")
      (loop []
        (when-let [entry (.getNextEntry stream)]
          (let [filename (str path "/" (.getName entry))]
            (if (.isDirectory entry)
              (do (info "Directory:" filename)
                  (.mkdir (io/file filename)))
              (do (info "File:" filename)
                  (io/copy stream (io/file filename))))
            (recur)))))))

(def clojure-tools-dir "ClojureTools")

(defn clojure-tools-url [version]
  (str "https://download.clojure.org/install/clojure-tools-" version ".zip"))

(defn download-clojure-tools [ctx]
  (info "Deleting directory" (str \" clojure-tools-dir \"))
  (delete-files-recursively clojure-tools-dir)
  (let [url (clojure-tools-url (:config/clojure-tools-version ctx))]
    (info "Downloading" url)
    (-> (client/get url {:as :byte-array}) :body io/input-stream (unzip-to "."))
    ctx))

(def wrapper-dir "Files")

(defn wrapper-url [version]
  (str "https://github.com/cark/clojure-win-cli-wrap/releases/download/" version "/clojure-win-cli-wrap.zip"))

(defn download-wrapper [ctx]
  (info "Deleting directory" (str \" wrapper-dir \"))
  (delete-files-recursively wrapper-dir)
  (let [url (wrapper-url (:config/wrapper-version ctx))]
    (info "Downloading" url)
    (-> (client/get url {:as :byte-array}) :body io/input-stream (unzip-to "Files"))
    ctx))

(def out-dir "out")

(defn build-installer [ctx]
  (info "Deleting directory" out-dir)
  (delete-files-recursively out-dir)  
  (let [command "make-installer.cmd"
        param (:config/clojure-tools-version ctx)
        _ (info "Launching Inno setup compiler")
        result (sh/sh command param)]
    (if (= 0 (:exit result))
      (do
        (logging/to-file-only #(report (:out result)))
        (logging/to-screen-only #(report "Success"))
        (info "And we're done ! Find the installer in the \"out\" directory.")
        ctx)
      (throw (ex-info "Error compiling the installer."
                      {:command [command param]
                       :expanded-error (str (:out result) "/n" (:err result))})))))

(defn -main [& args]
  (try
    (logging/setup-logging)
    (-> (get-config)
        download-clojure-tools
        download-wrapper
        build-installer)
    (when-not dev/*dev?*
      (System/exit 0))
    (catch Exception ex
      (logging/log-exception ex))))

