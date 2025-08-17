package com.test.findnearbyrestaurant.repository

import android.util.Log
import com.test.findnearbyrestaurant.data.RestaurantSearchResponse
import com.test.findnearbyrestaurant.retrofitApis.RetrofitProvider
import com.test.findnearbyrestaurant.utils.LogUtils

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
            radius = radius.coerceIn(100, 5000),
            sortBy = "distance",
            limit = 15,
            offset = offset
        )
        val location = if (useCoords) null else locationFallback
        LogUtils.showLog("Params",
            "Apis-Params: Authorization - $authHeader\n" +
                    "term - restaurants\n" +
                    "location - $location\n" +
                    "lat - $lat\n" +
                    "lng - $lng\n"+
                    "radius - $radius\n" +
                    "sortBy - distance\n" +
                    "limit - 15\n" +
                    "offset - $offset")
        return returnResponse
    }
}
