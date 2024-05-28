package com.example.wifigetdata

enum class Formats {
    CSV {
        override fun toFormat() = "csv"
        },
    JSON{
        override fun toFormat() = "json"
        },
    Excel{
        override fun toFormat() = "xlsx"
    };

    open fun toFormat(): String {
        return ""
    }

    companion object {
        fun toList() : List<Formats> {
            return listOf(CSV,JSON, Excel)
        }
    }
}