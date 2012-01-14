(ns netz.core
  (:use incanter.core))

(defn report-callback
  [network epoch mse complete]
  (if complete
    (println "Finished Epoch" epoch "MSE" mse)
    (println "Epoch" epoch "MSE" mse))
  true)

(def default-options
  {:max-epochs 20000 ; maximum number of epochs for training
   :desired-error 0.005 ; minimum desired error for training
   :learning-rate 0.25 ; learning rate for training, see docs
   :learning-momentum 0 ; learning momentum for training, see docs
   :callback report-callback ; called every epoch before weight adjustments
   :callback-resolution 100 ; how often to call callback (in no. epochs)
   :regularization-constant 0 ; regularization constant for training, see docs
   :calc-batch-error-in-parallel true}) ; calculate batch errors in parallel

(defn- network-option
  [network option-name]
  (or (option-name (:options network))
      (option-name default-options)))

(load "util")
(load "forward_prop")
(load "back_prop")
(load "gradient_descent")

(defprotocol NeuralNetwork
  (run [network inputs])
  (run-binary [network inputs])
  (train-on-examples [network examples]))

(defrecord MultiLayerPerceptron [weights options]
  NeuralNetwork
  (run [network inputs]
    (let [weights (:weights network)
          input-activations (matrix inputs)]
      (forward-propagate weights input-activations)))
  (run-binary [network inputs]
    (round-output (run network inputs)))
  (train-on-examples [network examples]
    (let [[first-input first-output] (first examples)
          num-inputs (length first-input)
          num-outputs (length first-output)
          hidden-neurons (or (:hidden-neurons (:options network))
                             (vec num-inputs))
          layer-sizes (conj (vec (cons num-inputs hidden-neurons)) num-outputs)
          network (assoc network :weights (random-weight-matrices layer-sizes))]
      (gradient-descent-backpropagation network examples))))

(defn train [examples & [options]]
  (let [network (MultiLayerPerceptron. nil options)]
    (train-on-examples network examples)))

(defn new-network [weights & [options]]
  (MultiLayerPerceptron. weights (or options {})))
