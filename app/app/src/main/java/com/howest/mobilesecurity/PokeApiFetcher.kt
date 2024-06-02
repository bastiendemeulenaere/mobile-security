package com.howest.mobilesecurity

import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

class PokeApiFetcher {
    companion object {
        private val logger: Logger = Logger.getLogger(PokeApiFetcher::class.java.name)
        fun fetchPokemonInfo(pokemonName: String, callback: (String?, String?) -> Unit) {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url("https://pokeapi.co/api/v2/pokemon/$pokemonName/")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val pokemonInfo = JSONObject(responseBody)
                        val pokemonHeight = pokemonInfo.getInt("height")
                        val pokemonWeight = pokemonInfo.getInt("weight")
                        val imageUrl =
                            pokemonInfo.getJSONObject("sprites").getString("front_default")

                        val pokemonAbilities = pokemonInfo.getJSONArray("abilities")
                        val abilitiesList = mutableListOf<String>()
                        for (i in 0 until pokemonAbilities.length()) {
                            val ability = pokemonAbilities.getJSONObject(i).getJSONObject("ability")
                                .getString("name")
                            abilitiesList.add(ability)
                        }
                        val abilitiesString = abilitiesList.joinToString(", ")

                        val pokemonInfoString =
                            "Name: $pokemonName\nHeight: $pokemonHeight\nWeight: $pokemonWeight\nAbilities: $abilitiesString"
                        callback(pokemonInfoString, imageUrl)
                    }else {
                        callback(null, null)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    logger.log(Level.SEVERE, "Request failed", e)
                    callback(null, null)
                }
            })
        }
    }
}
