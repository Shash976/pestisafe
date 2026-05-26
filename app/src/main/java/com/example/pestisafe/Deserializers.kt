package com.example.pestisafe

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type


data class PesticidesResponse(
    val pesticides: List<Pesticide>
)

data class MRLDetail(
    val pesticide: Pesticide,
    val commodity: Commodity,
    val mrl: Double
)

data class Detail(
    val id: Int,
    val pesticide: Pesticide,
    val mrls: List<MRLDetail>,
)


class PesticideDeserializer : JsonDeserializer<PesticidesResponse> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): PesticidesResponse {
        val jsonObject = json!!.asJsonObject
        val pesticidesArray = jsonObject.getAsJsonObject("pesticides").getAsJsonArray("pesticide").asJsonArray
        val pesticideList = mutableListOf<Pesticide>()
        pesticidesArray.forEach{ item ->
            try {
                val itemObj = item.asJsonObject
                if (!itemObj.has("id") || !itemObj.has("name")) {
                    Log.d("PestiSafe", "Skipping entry: missing 'id' or 'name' field")
                    return@forEach
                }
                val id = itemObj.get("id").asInt
                val nameObj = itemObj.get("name").asJsonObject
                if (!nameObj.has("en")) {
                    Log.d("PestiSafe", "Skipping entry: missing 'en' field in name object (id=$id)")
                    return@forEach
                }
                val name = nameObj.get("en").asString
                pesticideList.add(Pesticide(id, name))
            } catch (e: Exception) {
                Log.e("PestiSafe", "Skipping entry: ${e.message}")
            }
        }
        return PesticidesResponse(pesticideList)
    }
}

class DetailDeserializer : JsonDeserializer<Detail> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Detail {
        val jsonObject = json!!.asJsonObject
        if (!jsonObject.has("pestIdCodex")) {
            throw IllegalArgumentException("Missing required field 'pestIdCodex'")
        }
        val id = jsonObject.get("pestIdCodex").asInt
        if (!jsonObject.has("pesticide")) {
            throw IllegalArgumentException("Missing required field 'pesticide' for id=$id")
        }
        val pesticide = Pesticide(id, jsonObject.get("pesticide").asString)
        val mrlsArray = jsonObject.get("mrls").asJsonObject.getAsJsonArray("mrl")
        val mrlList = mutableListOf<MRLDetail>()
        mrlsArray.forEach{ mrlItem ->
            try {
                val mrlObj = mrlItem.asJsonObject
                val commodityObj = mrlObj.get("commodity").asJsonObject
                val commodity = Commodity(
                    name = commodityObj.get("name").asString,
                    id = commodityObj.get("id").asInt
                )
                val mrlElement = mrlObj.get("mrl")
                if (mrlElement == null || mrlElement.isJsonNull) {
                    Log.d("PestiSafe", "Skipping entry: mrl value is null for commodity '${commodity.name}'")
                    return@forEach
                }
                val mrlValue = try {
                    mrlElement.asDouble
                } catch (e: Exception) {
                    Log.e("PestiSafe", "Skipping entry: mrl value '${mrlElement.asString}' is not a valid number for commodity '${commodity.name}'")
                    return@forEach
                }
                mrlList.add(MRLDetail(pesticide, commodity, mrlValue))
            } catch (e: Exception) {
                Log.e("PestiSafe", "Skipping entry: ${e.message}")
            }
        }
        return Detail(id, pesticide, mrlList)
    }
}
