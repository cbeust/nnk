package com.beust.nnk

val LOG_LEVEL = 1

fun log(level: Int, s: String) {
    if (LOG_LEVEL >= level) println(s)
}

inline fun Int.times(apply: (Int) -> Unit) = (0..this - 1).forEach { apply(it) }

fun main(args: Array<String>) {
    log(1, "Running neural network xor()")
    xor()

    log(1, "Running neural network isOdd()")
    isOdd()

//    log(1, "Running neural network isOdd2()")
//    isOdd2()
}

fun isOdd2() {
    with(NeuralNetwork(1, 16, 1)) {
        val patterns = (0..100).map {
            NetworkData.create(listOf(it), listOf(it % 2))
        }
        train(patterns)

        val testPatterns = listOf(
            NetworkData.create(listOf(21), listOf(1)),
            NetworkData.create(listOf(32), listOf(0))
        )
        test(testPatterns)

        dump()
    }
}

fun isOdd() {
    with(NeuralNetwork(4, 2, 1)) {
        val patterns = listOf(
            NetworkData.create(listOf(0, 0, 0, 0), listOf(0)),
            NetworkData.create(listOf(0, 0, 0, 1), listOf(1)),
            NetworkData.create(listOf(0, 0, 1, 0), listOf(0)),
            NetworkData.create(listOf(0, 0, 1, 1), listOf(1)),
            NetworkData.create(listOf(0, 1, 0, 0), listOf(0)),
            NetworkData.create(listOf(0, 1, 0, 1), listOf(1)),
            NetworkData.create(listOf(0, 1, 1, 0), listOf(0)),
            NetworkData.create(listOf(0, 1, 1, 1), listOf(1))
        )
        train(patterns)

        val testPatterns = listOf(
            NetworkData.create(listOf(1, 0, 1, 1), listOf(1)),
            NetworkData.create(listOf(1, 1, 1, 0), listOf(0))
        )
        test(testPatterns)

        dump()
    }
}

fun xor() {
    with(NeuralNetwork(2, 2, 1)) {
        val patterns = listOf(
            NetworkData.create(listOf(0, 0), listOf(0)),
            NetworkData.create(listOf(0, 1), listOf(1)),
            NetworkData.create(listOf(1, 0), listOf(1)),
            NetworkData.create(listOf(1, 1), listOf(0)))
        train(patterns)
        test(patterns)
    }
}
