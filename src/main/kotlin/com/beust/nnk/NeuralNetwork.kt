package com.beust.nnk

import java.util.*

/**
 * A simple neural network with one hidden layer.
 */
class NeuralNetwork(val passedInput: Int, val hiddenSize: Int, val outputSize: Int) {

//    class Vector2(val size: Int, val defaultValue: () -> Float = { -> 0.0f }) : Matrix(size, 1, defaultValue) {
//        operator fun set(it: Int, value: Float) {
//            this[it][0] = value
//        }
//        override operator fun get(i: Int) = this[i][0]
//    }

    val random = Random(1)
    fun rand(min: Float, max: Float) = random.nextFloat() * (max - min) + min

    val inputSize = passedInput + 1 // Add one for the bias node

    // Activations for nodes
    val activationInput = Vector(inputSize, { -> 1.0f })
    val activationHidden = Vector(hiddenSize, { -> 1.0f })
    val activationOutput = Vector(outputSize, { -> 1.0f })

    // Weights
    val weightInput = Matrix(inputSize, hiddenSize, { -> rand(-0.2f, 0.2f) })
    val weightOutput = Matrix(hiddenSize, outputSize, { -> rand(-0.2f, 0.2f) })

    // Weights for momentum
    val momentumInput = Matrix(inputSize, hiddenSize)
    val momentumOutput = Matrix(hiddenSize, outputSize)

    // These two should probably be passed as a strategy so we can experiment with different
    // activation functions but hardcoding tanh for now
    fun sigmoid(x: Float) = Math.tanh(x.toDouble()).toFloat()
    fun sigmoidDerivative(x: Float) = 1.0f - x * x

    /**
     * Update the graph with the given inputs.
     * @return the outputs as a vector.
     */
    fun update(inputs: List<Float>, logLevel: Int) : Vector {
        if (inputs.size != inputSize -1) {
            throw RuntimeException("Expected ${inputSize - 1} inputs but got ${inputs.size}")
        }

        // Input activations (note: -1 since we don't count the bias node)
        repeat(inputSize - 1) {
            activationInput[it] = inputs[it]
        }

        // Hidden activations
        repeat(hiddenSize) { j ->
            var sum = 0.0f
            repeat(inputSize) { i ->
                val w: List<Float> = weightInput[i]
                log(logLevel, "    sum += ai[i] ${activationInput[i]} * wi[i][j] ${weightInput[i][j]}")
                sum += activationInput[i] * weightInput[i][j]
            }
            activationHidden[j] = sigmoid(sum)
            log(logLevel, "    final sum going into ah[$j]: " + activationHidden[j])
        }

        // Output activations
        repeat(outputSize) { k ->
            var sum = 0.0f
            repeat(hiddenSize) { j ->
                log(logLevel, "    sum += ah[$j] ${activationHidden[j]} * wo[$j][$k] ${weightOutput[j][k]}")
                log(logLevel, "         = " + activationHidden[j] * weightOutput[j][k])
                sum += activationHidden[j] * weightOutput[j][k]
            }
            log(logLevel, "  sigmoid(sum $sum) = " + sigmoid(sum))
            activationOutput[k] = sigmoid(sum)
        }

        return activationOutput
    }

    /**
     * Use the targets to backpropagate through the graph, starting with the output, then the hidden
     * layer and then the input.
     *
     * @return the error
     */
    fun backPropagate(targets: List<Float>, learningRate: Float, momentum: Float) : Float {
        if (targets.size != outputSize) {
            throw RuntimeException("Expected $outputSize targets but got ${targets.size}")
        }

        // Calculate error terms for output
        val outputDeltas = Vector(outputSize)
        repeat(outputSize) { k ->
            val error = targets[k] - activationOutput[k]
            outputDeltas[k] = sigmoidDerivative(activationOutput[k]) * error
        }

        // Calculate error terms for hidden layers
        val hiddenDeltas = Vector(hiddenSize)
        repeat(hiddenSize) { j ->
            var error = 0.0f
            repeat(outputSize) { k ->
                error += outputDeltas[k] * weightOutput[j][k]
            }
            hiddenDeltas[j] = sigmoidDerivative(activationHidden[j]) * error
        }

        // Update output weights
        repeat(hiddenSize) { j ->
            repeat(outputSize) { k ->
                val change = outputDeltas[k] * activationHidden[j]
                log(2, "      weightOutput[$j][$k] changing from " + weightOutput[j][k]
                    + " to " + (weightOutput[j][k] + learningRate * change + momentum * momentumOutput[j][k]))
                weightOutput[j][k] = weightOutput[j][k] + learningRate * change + momentum * momentumOutput[j][k]
                momentumOutput[j][k] = change
            }
        }

        // Update input weights
        repeat(inputSize) { i ->
            repeat(hiddenSize) { j ->
                val change = hiddenDeltas[j] * activationInput[i]
                log(2, "      weightInput[$i][$j] changing from " + weightInput[i][j]
                        + " to " + (weightInput[i][j] + learningRate * change + momentum * momentumInput[i][j]))
                weightInput[i][j] = weightInput[i][j] + learningRate * change + momentum * momentumInput[i][j]
                momentumInput[i][j] = change
            }
        }

        // Calculate error
        var error = 0.0
        repeat(targets.size) { k ->
            val diff = targets[k] - activationOutput[k]
            error += 0.5 * diff * diff
        }

        log(2, "      new error: " + error)
        return error.toFloat()
    }

    /**
     * Train the graph with the given NetworkData, which contains pairs of inputs and expected outputs.
     */
    fun train(networkDatas: List<NetworkData>, iterations: Int = 1000, learningRate: Float = 0.5f,
            momentum: Float = 0.1f) {
        repeat(iterations) { iteration ->
            var error = 0.0f
            networkDatas.forEach { pattern ->
                update(pattern.inputs, logLevel = 3)
                val bp = backPropagate(pattern.expectedOutputs, learningRate, momentum)
                error += bp
            }
            if (iteration % 100 == 0) {
                log(3, "  Iteration $iteration Error: $error")
            }
        }
    }

    fun test(networkDatas: List<NetworkData>) {
        networkDatas.forEach {
            log(1, it.inputs.toString() + " -> " + update(it.inputs, logLevel = 2))
        }
    }

    fun dump() {
        log(2, "Input weights:\n" + weightInput.dump())
        log(2, "Output weights:\n" + weightOutput.dump())
    }
}
