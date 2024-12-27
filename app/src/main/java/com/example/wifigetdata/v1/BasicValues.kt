package com.example.wifigetdata.v1

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
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
    private val updateTiming :MutableState<Long> = mutableLongStateOf(3000)

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

    fun resetValues(){
        voltageDataArray.clear()
        concentrationDataArray.clear()
        r2score.doubleValue = 0.0
        gradient.value = 0.0
        intercept.doubleValue = 0.0
    }
}
