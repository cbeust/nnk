package com.beust.nnk

import java.util.*

class Pattern(val inputs: List<Float>, val targets: List<Float>) {
    companion object {
        fun create(inputs: List<Int>, targets: List<Int>)
            = Pattern(inputs.map { it.toFloat() }, targets.map { it.toFloat() })
    }
}

val LOG_LEVEL = 1

fun log(level: Int, s: String) {
    if (LOG_LEVEL >= level) println(s)
}

fun main(args: Array<String>) {
    log(1, "Running neural network xor()")
    xor()

    log(1, "Running neural network isOdd()")
    isOdd()
}

fun isOdd() {
    with(NN(4, 2, 1)) {
        val patterns = listOf(
                Pattern.create(listOf(0, 0, 0, 0), listOf(0)),
                Pattern.create(listOf(0, 0, 0, 1), listOf(1)),
                Pattern.create(listOf(0, 0, 1, 0), listOf(0)),
                Pattern.create(listOf(0, 0, 1, 1), listOf(1)),
                Pattern.create(listOf(0, 1, 0, 0), listOf(0)),
                Pattern.create(listOf(0, 1, 0, 1), listOf(1)),
                Pattern.create(listOf(0, 1, 1, 0), listOf(0)),
                Pattern.create(listOf(0, 1, 1, 1), listOf(1))
        )
        val testPatterns = listOf(
                Pattern.create(listOf(1, 0, 1, 1), listOf(1)),
                Pattern.create(listOf(1, 1, 1, 0), listOf(0))
        )
        train(patterns)
        test(testPatterns)

        dump()
    }
}

fun xor() {
    with(NN(2, 2, 1)) {
        val patterns = listOf(
                Pattern.create(listOf(0, 0), listOf(0)),
                Pattern.create(listOf(0, 1), listOf(1)),
                Pattern.create(listOf(1, 0), listOf(1)),
                Pattern.create(listOf(1, 1), listOf(0)))
        train(patterns)
        test(patterns)
    }
}

fun range(n: Int) = (0..n-1)

class NN(val passedInput: Int, val nh: Int, val no: Int) {
    class Vector(val size: Int, val defaultValue: () -> Float = { -> 0.0f }) {
        val content = ArrayList<Float>(size)
        init {
            range(size).forEach {
                content.add(defaultValue())
            }
        }

        operator fun set(i: Int, value: Float) {
            content[i] = value
        }

        operator fun get(i: Int) = content[i]

        override fun toString() = content.toString()
    }

    open class Matrix(val rows: Int, val columns: Int, defaultValue: () -> Float = { -> 0.0f }) {
        val content = ArrayList<ArrayList<Float>>(rows * columns)

        init {
            range(rows).forEach { j ->
                val nl = ArrayList<Float>()
                content.add(nl)
                range(columns).forEach {
                    nl.add(defaultValue())
                }
            }
        }

        fun Float.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

        operator fun get(i: Int) = content[i]

        fun dump() : String {
            val result = StringBuilder()
            range(rows).forEach { i ->
                range(columns).forEach { j ->
                    result.append(content[i][j].format(2)).append(" ")
                }
                result.append("\n")
            }
            return result.toString()
        }
    }

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
    val ai = Vector(ni, { -> 1.0f} )
    val ah = Vector(nh, { -> 1.0f} )
    val ao = Vector(no, { -> 1.0f} )

    // Weights
    val wi = Matrix(ni, nh, { -> rand(-0.2f, 0.2f) })
    val wo = Matrix(nh, no, { -> rand(-0.2f, 0.2f) })

    // Weights for momentum
    val ci = Matrix(ni, nh)
    val co = Matrix(nh, no)

    fun sigmoid(x: Float) = Math.tanh(x.toDouble()).toFloat()

    fun sigmoidDerivative(x: Float) = 1.0f - x * x

    fun update(inputs: List<Float>) : Vector {
        if (inputs.size != ni -1) {
            throw RuntimeException("Expected ${ni - 1} inputs but got ${inputs.size}")
        }

        // Input activations (note: -1 since we don't count the bias node)
        range(ni - 1).forEach {
            ai[it] = inputs[it]
        }

        // Hidden activations
        range(nh).withIndex().forEach { iv ->
            val j = iv.index
            var sum = 0.0f
            range(ni).forEach { i ->
                val w: List<Float> = wi[i]
                log(2, "    sum += ai[i] ${ai[i]} * wi[i][j] ${wi[i][j]}")
                sum += ai[i] * wi[i][j]
            }
            ah[j] = sigmoid(sum)
            log(2, "    final sum going into ah[$j]: " + ah[j])
        }

        // Output activations
        range(no).forEach { k ->
            var sum = 0.0f
            range(nh).forEach { j ->
                log(2, "    sum += ah[$j] ${ah[j]} * wo[$j][$k] ${wo[j][k]}")
                log(2, "         = " + ah[j] * wo[j][k])
                sum += ah[j] * wo[j][k]
            }
            log(2, "  sigmoid(sum $sum) = " + sigmoid(sum))
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
        range(no).forEach { k ->
            val error = targets[k] - ao[k]
            outputDeltas[k] = sigmoidDerivative(ao[k]) * error
        }

        // Calculate error terms for hidden layers
        val hiddenDeltas = Vector(nh)
        range(nh).forEach { j ->
            var error = 0.0f
            range(no).forEach { k ->
                error += outputDeltas[k] * wo[j][k]
            }
            hiddenDeltas[j] = sigmoidDerivative(ah[j]) * error
        }

        // Update output weights
        range(nh).forEach { j ->
            range(no).forEach { k ->
                val change = outputDeltas[k] * ah[j]
                wo[j][k] = wo[j][k] + learningRate * change + momentum * co[j][k]
                co[j][k] = change
            }
        }

        // Update input weights
        range(ni).forEach { i ->
            range(nh).forEach { j ->
                val change = hiddenDeltas[j] * ai[i]
                wi[i][j] = wi[i][j] + learningRate * change + momentum * ci[i][j]
                ci[i][j] = change
            }
        }

        // Calculate error
        var error = 0.0
        range(targets.size).forEach { k ->
            val diff = targets[k] - ao[k]
            error += 0.5 * diff * diff
        }

        return error.toFloat()
    }

    fun train(patterns: List<Pattern>, iterations: Int = 1000, learningRate: Float = 0.5f,
            momentum: Float = 0.1f) {
        range(iterations).forEach { iteration ->
            var error = 0.0f
            patterns.forEach { pattern ->
                update(pattern.inputs)
                val bp = backPropagate(pattern.targets, learningRate, momentum)
                error += bp
            }
            if (iteration % 100 == 0) {
                log(2, "  Iteration $iteration Error: $error")
            }
        }
    }

    fun test(patterns: List<Pattern>) {
        patterns.forEach {
            log(1, it.inputs.toString() + " -> " + update(it.inputs))
        }
    }

    fun dump() {
        log(2, "Input weights:\n" + wi.dump())
        log(2, "Output weights:\n" + wo.dump())
    }
}