package com.beust.nnk

val LOG_LEVEL = 1

fun log(level: Int, s: String) {
    if (LOG_LEVEL >= level) println(s)
}

fun main(args: Array<String>) {
    log(1, "Running neural network xor()")
    xor()

    log(1, "Running neural network isOdd()")
    isOdd()

//    log(1, "Running neural network isOdd2()")
//    isOdd2()
}

fun isOdd2() {
    with(NeuralNetwork(inputSize = 1, hiddenSize = 16, outputSize = 1)) {
        val trainingValues = (0..100).map {
            NetworkData.create(listOf(it), listOf(it % 2))
        }
        train(trainingValues)

        val testValues = listOf(
            NetworkData.create(listOf(21), listOf(1)),
            NetworkData.create(listOf(32), listOf(0))
        )
        test(testValues)

        dump()
    }
}

fun isOdd() {
    with(NeuralNetwork(inputSize = 4, hiddenSize = 2, outputSize = 1)) {
        val trainingValues = listOf(
            NetworkData.create(listOf(0, 0, 0, 0), listOf(0)),
            NetworkData.create(listOf(0, 0, 0, 1), listOf(1)),
            NetworkData.create(listOf(0, 0, 1, 0), listOf(0)),
            NetworkData.create(listOf(0, 1, 1, 0), listOf(0)),
            NetworkData.create(listOf(0, 1, 1, 1), listOf(1)),
            NetworkData.create(listOf(1, 0, 1, 0), listOf(0)),
            NetworkData.create(listOf(1, 0, 1, 1), listOf(1)),
            NetworkData.create(listOf(1, 1, 0, 0), listOf(0)),
            NetworkData.create(listOf(1, 1, 0, 1), listOf(1)),
            NetworkData.create(listOf(1, 1, 1, 0), listOf(0)),
            NetworkData.create(listOf(1, 1, 1, 1), listOf(1))
        )
        train(trainingValues)

        val testValues = listOf(
            NetworkData.create(listOf(0, 0, 1, 1), listOf(1)),
            NetworkData.create(listOf(0, 1, 0, 0), listOf(0)),
            NetworkData.create(listOf(0, 1, 0, 1), listOf(1)),
            NetworkData.create(listOf(1, 0, 0, 0), listOf(0)),
            NetworkData.create(listOf(1, 0, 0, 1), listOf(1))
        )
        test(testValues)

        dump()
    }
}

fun xor() {
    with(NeuralNetwork(inputSize = 2, hiddenSize = 2, outputSize = 1)) {
        val trainingValues = listOf(
            NetworkData.create(listOf(0, 0), listOf(0)),
            NetworkData.create(listOf(0, 1), listOf(1)),
            NetworkData.create(listOf(1, 0), listOf(1)),
            NetworkData.create(listOf(1, 1), listOf(0)))
        train(trainingValues)
        test(trainingValues)
    }
}
