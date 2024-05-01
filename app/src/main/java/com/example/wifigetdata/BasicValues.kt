package com.example.wifigetdata


object BasicValues {
    private var receivedVal :Double = 0.0
    private var r2score :Double = 0.0
    private var url :String = ""
    private var calibrationConcentration = doubleArrayOf( 1.0, 10.0, 5.0, 7.5, 6.0, 2.5, 4.0, 1.25)
    private var voltageDataArray  = mutableListOf<Double>(1.0, 2.0)
    private var concentrationDataArray = mutableListOf<Double>(1.0, 3.0)
    private var gradient :Double= 0.0
    private var intercept :Double = 0.0

    fun getGradient() :Double {
        return gradient
    }

    fun getIntercept() :Double {
        return intercept
    }

    fun setGradient(newGradient: Double) {
        gradient = newGradient
    }

    fun setIntercept(newIntercept :Double) {
        intercept = newIntercept
    }

    fun getCalibrationConcentration(): DoubleArray {
        return calibrationConcentration
    }

    fun updateData(newVoltage :Double, newConcentration :Double) {
        voltageDataArray.add(newVoltage)
        concentrationDataArray.add(newConcentration)
    }

    fun getVoltageDataArray() :MutableList<Double> {
        return concentrationDataArray
    }

    fun getConcentrationDataArray() :MutableList<Double> {
        return concentrationDataArray
    }

    fun setURL(newURL:String){
        url = newURL
    }

    fun getURL() :String {
        return url
    }

    fun getReceivedVal(): Double {
        return receivedVal
    }
    fun setReceivedVal(value:Double) {
        receivedVal = value
    }
    fun getR2Score() :Double{
        return r2score
    }
    fun setR2Score(value: Double){
        r2score = value
    }
}
