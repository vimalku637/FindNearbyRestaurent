package com.test.findnearbyrestaurant.retrofitApis

import com.test.findnearbyrestaurant.data.RestaurantSearchResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface RetrofitApiService {
    @GET("businesses/search")
    suspend fun searchRestaurants(
        @Header("Authorization") auth: String,
        @Query("term") term: String = "restaurants",
        @Query("location") location: String? = null,
        @Query("latitude") lat: Double? = null,
        @Query("longitude") lng: Double? = null,
        @Query("radius") radius: Int = 500, // make default 500 meters // range 100..5000 (in meters)
        @Query("sort_by") sortBy: String = "distance",
        @Query("limit") limit: Int = 15,
        @Query("offset") offset: Int = 0
    ): RestaurantSearchResponse
}