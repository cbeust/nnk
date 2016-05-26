package com.beust.nnk

import java.util.*

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

    operator fun get(i: Int) : Float = content[i]

    override fun toString() = content.toString()
}