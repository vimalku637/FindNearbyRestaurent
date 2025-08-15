package com.test.findnearbyrestaurant.repository

import com.test.findnearbyrestaurant.data.RestaurantSearchResponse
import com.test.findnearbyrestaurant.retrofitApis.RetrofitProvider

class RestaurantRepository(private val apiKey: String) {
    private val authHeader get() = "Bearer $apiKey"

    suspend fun search(
        useCoords: Boolean,
        lat: Double?,
        lng: Double?,
        locationFallback: String?,
        radius: Int,
        offset: Int
    ): RestaurantSearchResponse {
        val returnResponse = RetrofitProvider.api.searchRestaurants(
            auth = authHeader,
            term = "restaurants",
            location = if (useCoords) null else locationFallback,
            lat = if (useCoords) lat else null,
            lng = if (useCoords) lng else null,
            radius = radius.coerceIn(500, 5000),
            sortBy = "distance",
            limit = 15,
            offset = offset
        )
        return returnResponse
    }
}
