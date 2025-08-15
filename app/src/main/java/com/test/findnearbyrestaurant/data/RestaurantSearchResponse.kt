package com.test.findnearbyrestaurant.data

data class RestaurantSearchResponse(
    val businesses: List<Business> = emptyList(),
    val total: Int = 0
)
