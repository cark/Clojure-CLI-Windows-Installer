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

(s/def :install.edn/portable-cli-version string?)
(s/def :install.edn/clojure-version string?)
(s/def :install.edn/file (s/keys :req-un [:install.edn/clojure-version :install.edn/portable-cli-version]))

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
      (set/rename-keys {:portable-cli-version :config/portable-cli-version
                        :clojure-version :config/clojure-version})))

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
                  (io/make-parents (io/file filename))
                  (io/copy stream (io/file filename))))
            (recur)))))))

(def portable-cli-dir "clojure-cli")

(defn portable-cli-url [ctx]
  (str "https://github.com/cark/clojure-cli-portable/releases/download/v" (:config/portable-cli-version ctx)
       "/clojure-cli-win-" (:config/clojure-version ctx) ".zip"))

(defn download-portable-cli [ctx]
  (info "Deleting directory" (str \" portable-cli-dir \"))
  (delete-files-recursively portable-cli-dir)
  (let [url (portable-cli-url ctx)]
    (info "Downloading" url)
    (-> (client/get url {:as :byte-array}) :body io/input-stream (unzip-to "."))
    ctx))

(def out-dir "out")

(defn build-installer [ctx]
  (info "Deleting directory" out-dir)
  (delete-files-recursively out-dir)
  (.mkdir (io/file out-dir))
  (let [command "nsis.cmd"
        param (:config/clojure-version ctx)
        _ (info "Launching Setup compiler")
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
        download-portable-cli
        build-installer)
    (when-not dev/*dev?*
      (System/exit 0))
    (catch Exception ex
      (logging/log-exception ex))))

