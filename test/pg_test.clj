(ns dsql.pg-test
  (:require [dsql.pg :as sut]
            [clojure.test :refer [deftest is]]))

(defmacro format= [q patt]
  `(let [res# (sut/format ~q)]
     (is (= ~patt res#))
     res#))

(deftest test-dsql-pgi

  (format=
   {:select :*
    :from :user
    :where ^:pg/op[:= :user.id [:pg/param "u-1"]]
    :limit 100}
   ["SELECT * FROM user WHERE user.id = ? LIMIT 100" "u-1"])

  (format=
   {:select :*
    :from :user
    :where ^:pg/op[:= :user.id "u-1"]
    :limit 100}
   ["SELECT * FROM user WHERE user.id = 'u-1' LIMIT 100"])

  (format=
   {:ql/type :pg/and
    :by-id ^:pg/op[:= :id [:pg/param "u-1"]]
    :by-status ^:pg/op[:= :status "active"]}
   ["/*by-id*/ id = ? AND /*by-status*/ status = 'active'" "u-1"])

  (format=
   {:ql/type :pg/projection
    :id :id
    :raw [:pg/sql "current_time()"]
    :fn ^:pg/fn[:current_time "1" [:pg/param "x"]]}
   ["id as id , current_time() as raw , current_time( '1' , ? ) as fn" "x"])

  (format=
   {:select {:resource ^:pg/op[:|| :resource ^:pg/obj{:id :id :resourceType "Patient"}]}
    :from {:pt :patient}
    :where {:match
            ^:pg/op[:&& ^:pg/fn[:knife_extract_text :resource ^:pg/jsonb[["match"]]]
                    [:pg/array-param :text ["a-1" "b-2"]]]}
    :limit 10}
   ["SELECT resource || jsonb_build_object( id , id , resourceType , 'Patient' ) as resource FROM patient pt WHERE /*match*/ knife_extract_text( resource , '[[\"match\"]]' ) && ?::text[] LIMIT 10" "{\"a-1\",\"b-2\"}"])




  )
