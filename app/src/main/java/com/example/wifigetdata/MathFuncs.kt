package com.example.wifigetdata

import kotlin.math.sqrt

fun linearRegression(x: List<Double>, y: List<Double>): Pair<Double, Double> {
    if (x.size != y.size) {
        throw IllegalArgumentException("Lists must have the same size")
    }

    val n = x.size
    val sumX = x.sum()
    val sumY = y.sum()
    val sumXY = x.zip(y) { a, b -> a * b }.sum()
    val sumX2 = x.sumOf { it * it }

    val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
    val intercept = (sumY - (slope * sumX)) / n

    return Pair(slope, intercept)
}


fun calcSlopeIntercept(x1: Double, y1: Double, x2: Double, y2: Double): Pair<Double, Double> {
    val gradient = (y2 - y1) / (x2 - x1)
    val intercept = y1 - (gradient * x1)
    return Pair(gradient,intercept)
}

fun calculateRSquared(array1: DoubleArray, array2: DoubleArray): Double {
    if (array1.size != array2.size) {
        throw IllegalArgumentException("Arrays must have the same size")
    }

    val n = array1.size
    val mean1 = array1.average()
    val mean2 = array2.average()

    var numerator = 0.0
    var denominator1 = 0.0
    var denominator2 = 0.0

    for (i in 0 until n) {
        val diff1 = array1[i] - mean1
        val diff2 = array2[i] - mean2
        numerator += diff1 * diff2
        denominator1 += diff1 * diff1
        denominator2 += diff2 * diff2
    }

    val r = numerator / sqrt(denominator1 * denominator2)
    return r * r
}
