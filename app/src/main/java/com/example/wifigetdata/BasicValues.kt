package com.example.wifigetdata

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow


object BasicValues {
    val receivedVal = MutableStateFlow(0.0)
    private val r2score = mutableDoubleStateOf(0.0)
    private val url = MutableStateFlow("")
    private val calibrationConcentration = doubleArrayOf( 1.0, 10.0, 5.0, 7.5, 6.0, 2.5, 4.0, 1.25)
    private var voltageDataArray  = mutableListOf<Double>()
    private var concentrationDataArray = mutableListOf<Double>()
    private val gradient : MutableState<Double> = mutableDoubleStateOf(0.0)
    private val intercept = mutableDoubleStateOf(0.0)
    private val updateTiming :MutableState<Long> = mutableLongStateOf(1500)

    fun getGradient() :Double {
        return gradient.value
    }

    fun getIntercept() :Double {
        return intercept.doubleValue
    }

    fun setGradient(newGradient: Double) {
        gradient.value = newGradient
    }

    fun setIntercept(newIntercept :Double) {
        intercept.doubleValue = newIntercept
    }

    fun getCalibrationConcentration(): DoubleArray {
        return calibrationConcentration
    }

    fun updateData(newVoltage :Double, newConcentration :Double) {
        voltageDataArray.add(newVoltage)
        concentrationDataArray.add(newConcentration)

        var array1= concentrationDataArray
        var array2 = voltageDataArray

        val pairList = array1.zip(array2).toList()
        val sortedPairList = pairList.sortedBy { it.first }

        array1 = sortedPairList.map { it.first }.toMutableList()
        array2 = sortedPairList.map { it.second }.toMutableList()

        voltageDataArray = array2
        concentrationDataArray = array1

        if (voltageDataArray.size > 2) {
            updateGradientIntercept()
        }
    }

    fun getVoltageDataArray() :MutableList<Double> {
        return voltageDataArray
    }

    fun getConcentrationDataArray() :MutableList<Double> {
        return concentrationDataArray
    }

    fun setURL(newURL:String){
        url.value= newURL
    }

    fun getURL() :String {
        return url.value
    }

    fun getReceivedVal(): Double {
        return receivedVal.value
    }
    fun setReceivedVal(value:Double) {
        receivedVal.value = value
    }
    fun getR2Score() :Double{
        return r2score.doubleValue
    }
    fun setR2Score(value: Double){
        r2score.doubleValue = value
    }

    fun getUpdateTiming() :Long {
        return updateTiming.value
    }
}
