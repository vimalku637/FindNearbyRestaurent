package com.test.findnearbyrestaurant.data

import com.google.gson.annotations.SerializedName

data class Business(
    val id: String,
    val name: String,
    val distance: Double,
    val rating: Double,
    val image_url: String?,
    @SerializedName("is_closed")
    val isClosed: Boolean,
    val location: Location? = null
)
