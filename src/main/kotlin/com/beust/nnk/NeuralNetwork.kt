package com.beust.nnk

import java.util.*

class NeuralNetwork(val passedInput: Int, val nh: Int, val no: Int) {

//    class Vector2(val size: Int, val defaultValue: () -> Float = { -> 0.0f }) : Matrix(size, 1, defaultValue) {
//        operator fun set(it: Int, value: Float) {
//            this[it][0] = value
//        }
//        override operator fun get(i: Int) = this[i][0]
//    }

    val random = Random(1)
    fun rand(min: Float, max: Float) = random.nextFloat() * (max - min) + min

    val ni = passedInput + 1 // Add one for the bias node

    // Activations for nodes
    val ai = Vector(ni, { -> 1.0f })
    val ah = Vector(nh, { -> 1.0f })
    val ao = Vector(no, { -> 1.0f })

    // Weights
    val wi = Matrix(ni, nh, { -> rand(-0.2f, 0.2f) })
    val wo = Matrix(nh, no, { -> rand(-0.2f, 0.2f) })

    // Weights for momentum
    val ci = Matrix(ni, nh)
    val co = Matrix(nh, no)

    fun sigmoid(x: Float) = Math.tanh(x.toDouble()).toFloat()

    fun sigmoidDerivative(x: Float) = 1.0f - x * x

    fun update(inputs: List<Float>, logLevel: Int) : Vector {
        if (inputs.size != ni -1) {
            throw RuntimeException("Expected ${ni - 1} inputs but got ${inputs.size}")
        }

        // Input activations (note: -1 since we don't count the bias node)
        repeat(ni - 1) {
            ai[it] = inputs[it]
        }

        // Hidden activations
        repeat(nh) { j ->
            var sum = 0.0f
            repeat(ni) { i ->
                val w: List<Float> = wi[i]
                log(logLevel, "    sum += ai[i] ${ai[i]} * wi[i][j] ${wi[i][j]}")
                sum += ai[i] * wi[i][j]
            }
            ah[j] = sigmoid(sum)
            log(logLevel, "    final sum going into ah[$j]: " + ah[j])
        }

        // Output activations
        repeat(no) { k ->
            var sum = 0.0f
            repeat(nh) { j ->
                log(logLevel, "    sum += ah[$j] ${ah[j]} * wo[$j][$k] ${wo[j][k]}")
                log(logLevel, "         = " + ah[j] * wo[j][k])
                sum += ah[j] * wo[j][k]
            }
            log(logLevel, "  sigmoid(sum $sum) = " + sigmoid(sum))
            ao[k] = sigmoid(sum)
        }

        return ao
    }

    /**
     * @return the error
     */
    fun backPropagate(targets: List<Float>, learningRate: Float, momentum: Float) : Float {
        if (targets.size != no) {
            throw RuntimeException("Expected $no targets but got ${targets.size}")
        }

        // Calculate error terms for output
        val outputDeltas = Vector(no)
        repeat(no) { k ->
            val error = targets[k] - ao[k]
            outputDeltas[k] = sigmoidDerivative(ao[k]) * error
        }

        // Calculate error terms for hidden layers
        val hiddenDeltas = Vector(nh)
        repeat(nh) { j ->
            var error = 0.0f
            repeat(no) { k ->
                error += outputDeltas[k] * wo[j][k]
            }
            hiddenDeltas[j] = sigmoidDerivative(ah[j]) * error
        }

        // Update output weights
        repeat(nh) { j ->
            repeat(no) { k ->
                val change = outputDeltas[k] * ah[j]
                wo[j][k] = wo[j][k] + learningRate * change + momentum * co[j][k]
                co[j][k] = change
            }
        }

        // Update input weights
        repeat(ni) { i ->
            repeat(nh) { j ->
                val change = hiddenDeltas[j] * ai[i]
                wi[i][j] = wi[i][j] + learningRate * change + momentum * ci[i][j]
                ci[i][j] = change
            }
        }

        // Calculate error
        var error = 0.0
        repeat(targets.size) { k ->
            val diff = targets[k] - ao[k]
            error += 0.5 * diff * diff
        }

        return error.toFloat()
    }

    fun train(networkDatas: List<NetworkData>, iterations: Int = 1000, learningRate: Float = 0.5f,
            momentum: Float = 0.1f) {
        repeat(iterations) { iteration ->
            var error = 0.0f
            networkDatas.forEach { pattern ->
                update(pattern.inputs, 3)
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
            log(1, it.inputs.toString() + " -> " + update(it.inputs, 2))
        }
    }

    fun dump() {
        log(2, "Input weights:\n" + wi.dump())
        log(2, "Output weights:\n" + wo.dump())
    }
}
