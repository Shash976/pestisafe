package com.example.pestisafe

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
            val id = item.asJsonObject.get("id").asInt
            val name = item.asJsonObject.get("name").asJsonObject.get("en").asString
            pesticideList.add(Pesticide(id, name))
        }
        return PesticidesResponse(pesticideList)
    }
}

class DetailDeserializer : JsonDeserializer<Detail> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Detail {
        println(json)
        val jsonObject = json!!.asJsonObject
        val id = jsonObject.get("pestIdCodex").asInt
        println(id)
        val pesticide = Pesticide(id, jsonObject.get("pesticide").asString)
        val mrlsArray = jsonObject.get("mrls").asJsonObject.getAsJsonArray("mrl")
        val mrlList = mutableListOf<MRLDetail>()
        mrlsArray.forEach{ mrlItem ->
            val commodity = Commodity(name=mrlItem.asJsonObject.get("commodity").asJsonObject.get("name").asString, id=mrlItem.asJsonObject.get("commodity").asJsonObject.get("id").asInt)
            val mrl = mrlItem.asJsonObject.get("mrl")
            try{
                println(mrl)
                mrlList.add(MRLDetail(pesticide, commodity, mrl.asDouble))
            } catch (e: Exception){
                println(e)
            }
        }
        return Detail(id, pesticide, mrlList)
    }
}