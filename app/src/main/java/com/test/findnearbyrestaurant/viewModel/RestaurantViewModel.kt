package com.test.findnearbyrestaurant.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.findnearbyrestaurant.data.UiState
import com.test.findnearbyrestaurant.repository.RestaurantRepository
import com.test.findnearbyrestaurant.utils.LogUtils
import kotlinx.coroutines.launch

class RestaurantViewModel(private val repository: RestaurantRepository) : ViewModel() {

    private val _state = MutableLiveData(UiState())
    val state: LiveData<UiState> = _state

    fun setRadius(r: Int) {
        LogUtils.showLog("Radius", "setRadius: $r")
        _state.value = _state.value?.copy(radius = r.coerceIn(100, 5000))
    }

    fun setLocation(lat: Double?, lng: Double?, useCoords: Boolean) {
        LogUtils.showLog("Location", "setLocation: $lat - $lng -> $useCoords")
        _state.value = _state.value?.copy(lat = lat, lng = lng, useCoords = useCoords)
    }

    fun refreshListItems(locationFallback: String = "New York City") {
        load(reset = true, locationFallback = locationFallback)
    }

    fun loadMoreListItems(locationFallback: String = "New York City") {
        load(reset = false, locationFallback = locationFallback)
    }

    private fun load(reset: Boolean, locationFallback: String) {
        val curr = _state.value ?: return
        if (curr.isLoading || curr.isLoadingMore) return

        LogUtils.showLog("Current Offset", "load: "+curr.offset)

        val newOffset = if (reset) 0 else curr.offset

        LogUtils.showLog("New Offset", "load: $newOffset")

        _state.value = curr.copy(
            isLoading = reset,
            isLoadingMore = !reset,
            error = null,
            offset = newOffset)

        viewModelScope.launch {
            try {
                val response = repository.search(
                    useCoords = curr.useCoords,
                    lat = curr.lat,
                    lng = curr.lng,
                    locationFallback = locationFallback,
                    radius = curr.radius,
                    offset = newOffset
                )
                LogUtils.showLog("API Response", "Full response: ${response.businesses}")
                val merged = if (reset) response.businesses else curr.items + response.businesses
                _state.value = curr.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    items = merged,
                    total = response.total,
                    offset = newOffset + 15
                )
            } catch (e: Exception) {
                LogUtils.showLog("Error", "Error fetching restaurants: ${e.message}")
                _state.value = curr.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message ?: "Unknown error")
            }
        }
    }
}