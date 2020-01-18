package com.example.app_22_testmapviewpager2.architecture

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CrazyagroApi {

    @GET("discussion")
    fun getDiscussions(@Query("lat") lat : Float, @Query("lng") lng : Float, @Query("radiusKm") radius : Int) : Call<Array<Discussions>>

}