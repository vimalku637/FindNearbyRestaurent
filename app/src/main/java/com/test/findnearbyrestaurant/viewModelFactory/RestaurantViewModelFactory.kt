package com.test.findnearbyrestaurant.viewModelFactory

import androidx.lifecycle.ViewModelProvider
import com.test.findnearbyrestaurant.repository.RestaurantRepository
import com.test.findnearbyrestaurant.viewModel.RestaurantViewModel

class RestaurantViewModelFactory(private val repo: RestaurantRepository) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RestaurantViewModel(repo) as T
    }
}
