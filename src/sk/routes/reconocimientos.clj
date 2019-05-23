(ns sk.routes.registro
  (:require [cheshire.core :refer [generate-string]]
            [ring.handler.dump :refer :all]
            [noir.response :refer [redirect]]
            [noir.session :as session]
            [selmer.parser :refer [render render-file]]
            [sk.models.crud :refer [db Query Save Update]]
            [sk.models.email :refer [host send-email]]
            [sk.models.util :refer [format-date-internal
                                    get-matricula-id
                                    capitalize-words
                                    get-reset-url
                                    create-token
                                    check-token]))