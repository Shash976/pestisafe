package com.example.wifigetdata

import kotlin.math.sqrt

fun updateConcentration() {
    val voltage = BasicValues.getReceivedVal()
    val concentration = calculateConcentration(voltage)
    BasicValues.updateData(voltage, concentration)
}

fun calculateConcentration(voltage: Double): Double {
    // Y = MX + C -> X = Y-C / M
    return (voltage - BasicValues.getIntercept()) / BasicValues.getGradient()
}

fun updateGradientIntercept() {
    val size  = BasicValues.getConcentrationDataArray().size
    if (size > 2) {
        val x1 = BasicValues.getConcentrationDataArray()[size-2]
        val x2 = BasicValues.getConcentrationDataArray()[size-1]
        val y1 = BasicValues.getVoltageDataArray()[size-2]
        val y2 = BasicValues.getVoltageDataArray()[size-1]
        val (gradient, intercept) = calcSlopeIntercept(x1, y1, x2, y2)
        BasicValues.setGradient(gradient)
        BasicValues.setIntercept(intercept)
    }
}

fun calcSlopeIntercept(x1: Double, y1: Double, x2: Double, y2: Double): Pair<Double, Double> {
    val gradient = (y2 - y1) / (x2 - x1)
    val intercept = y1 - (gradient * x1)
    return gradient to intercept
}

suspend fun updateR2Score() {
    val r2score = calculateRSquared(
        BasicValues.getVoltageDataArray().toDoubleArray(),
        BasicValues.getConcentrationDataArray().toDoubleArray()
    )
    BasicValues.setR2Score(r2score)
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
