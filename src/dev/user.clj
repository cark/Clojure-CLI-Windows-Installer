(ns user
  (:require [clj-install.dev :as dev]))

(alter-var-root #'dev/*dev?* (constantly true))
