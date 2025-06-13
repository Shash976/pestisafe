package com.example.pestisafe

import kotlin.math.sqrt

fun linearRegression(X: List<Double>, Y: List<Double>): Pair<Double, Double> {
    /**
     * Calculate the linear regression of two lists of numbers.
     */
    if (X.size != Y.size) {
        throw IllegalArgumentException("Lists must have the same size")
    }
    val sortedPairs = X.zip(Y).sortedBy { it.first }
    val x = sortedPairs.map { it.first }
    val y = sortedPairs.map { it.second }

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
    /**
     * Calculate the R-squared value of two arrays of numbers.
     */
    if (array1.size != array2.size) {
        throw IllegalArgumentException("Arrays must have the same size")
    }

    val n = array1.size // number of elements in the array
    val mean1 = array1.average() // mean of the first array
    val mean2 = array2.average() // mean of the second array

    var numerator = 0.0 // numerator of the correlation coefficient
    var denominator1 = 0.0 // denominator of the correlation coefficient for array1
    var denominator2 = 0.0 // denominator of the correlation coefficient for array2

    // Calculate the correlation coefficient
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
