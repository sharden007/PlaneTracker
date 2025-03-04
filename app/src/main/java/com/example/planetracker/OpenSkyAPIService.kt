package com.example.planetracker
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenSkyApiService {
    @GET("states/all")
    fun getAllStates(@Query("lamin") lamin: Double, @Query("lomin") lomin: Double,
                     @Query("lamax") lamax: Double, @Query("lomax") lomax: Double):
            Call<OpenSkyResponse>
}
