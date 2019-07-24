(ns clj-install.logging
  (:require [taoensso.timbre :as timbre
             :refer [log  trace  debug  info  warn  error  fatal  report
                     logf tracef debugf infof warnf errorf fatalf reportf
                     spy get-env]]
            [taoensso.timbre.appenders.core :as appenders]
            [clj-install.dev :as dev]))

(defonce logfile-name (atom ""))

(defn generate-logfile-name []
  (let [current-directory (System/getProperty "user.dir")
        log-dir (str current-directory java.io.File/separatorChar)
        now (java.time.LocalDateTime/now)
        formatter (.toFormat (java.time.format.DateTimeFormatter/ofPattern "yyyMMdd-HHmmss-SSSS"))]
    (str "logs" java.io.File/separator (.format formatter now) ".log")))

(defn setup-logging []
  ;;disable clj-http spam
  (System/setProperty "org.apache.commons.logging.Log" "org.apache.commons.logging.impl.NoOpLog")
  (let [filename (generate-logfile-name)]
    (reset! logfile-name filename)
    (timbre/merge-config!
     {:appenders {:spit (appenders/spit-appender {:fname filename})}
      :output-fn (partial timbre/default-output-fn {:stacktrace-fonts {}})})))

(defn log-exception [ex]
  (fatal (.getMessage ex))
  (when-let [info (ex-data ex)]
    (fatal (dissoc info :expanded-error)))
  (timbre/with-config (update-in timbre/*config* [:appenders] dissoc :spit)
    (fatal "Details in" @logfile-name))
  (timbre/with-config (update-in timbre/*config* [:appenders] dissoc :println)
    (fatal ex)
    (when-let [info (:expanded-error (ex-data ex))]
      (fatal info)))
  (if dev/*dev?*
    (throw ex)
    (System/exit 1)))
