(ns app.core)

  (use '[clojure.java.shell :only [sh]])

  (def term_height (read-string (clojure.string/trim (:out (sh "tput" "lines")))))
  (def term_width (read-string (clojure.string/trim (:out (sh "tput" "cols")))))


  (require '[clj-http.client :as http])

  (def spy_2013_url "http://ichart.finance.yahoo.com/table.csv?s=SPY&d=3&e=28&f=2013&g=d&a=0&b=1&c=2013&ignore=.csv")
  (def response (http/get spy_2013_url))
  (def response_body (response :body))
  (def spy_stats (filter #(not= "" %1) (rest (clojure.string/split response_body #"\n"))))
  (def adj_px (take-last term_width (map #(read-string ((clojure.string/split %1 #",") 6)) spy_stats)))


  (def px_max (apply max adj_px))
  (def px_min (apply min adj_px))

  (def scale (/ term_height (- px_max px_min)))
  (defn floor [n] (.setScale (bigdec n) 0 java.math.RoundingMode/FLOOR))

  (def plot_positions (map #(floor (* (- %1 px_min) scale)) adj_px))

  (doseq [row (range (- term_height 1) -1 -1)]
    (doseq [col (range (- term_width 1) -1 -1)]
      (if (== row (nth plot_positions col)) (print "*") (print " "))
      (comment if (== row (nth plot_positions col)) (print (nth adj_px col) " "))
    )
    (println)
  )
  
