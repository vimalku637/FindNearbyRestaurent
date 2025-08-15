package com.test.findnearbyrestaurant.data

data class UiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<Business> = emptyList(),
    val total: Int = 0,
    val error: String? = null,
    val radius: Int = 500,
    val useCoords: Boolean = false,
    val lat: Double? = null,
    val lng: Double? = null,
    val offset: Int = 0
)
