package com.test.findnearbyrestaurant

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.test.findnearbyrestaurant.adapter.FindNearbyRestaurantsAdapter
import com.test.findnearbyrestaurant.databinding.ActivityMainBinding
import com.test.findnearbyrestaurant.viewModelFactory.RestaurantViewModelFactory
import com.test.findnearbyrestaurant.repository.RestaurantRepository
import com.test.findnearbyrestaurant.utils.LogUtils
import com.test.findnearbyrestaurant.viewModel.RestaurantViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: RestaurantViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var findNearbyRestaurantsAdapter : FindNearbyRestaurantsAdapter

    private val API_KEY = "ZOE9c5S19iMlTt2kBa8nkVuRnya7pTfw9d2srzeyWnceY9_HdEB7eVBg6Rgelcm1lWGkejFWTL_n_R_W1rdLIYwEdIQGsfuFX3knVMAFaog7AvhLGKerIZWhVQOeaHYx"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel = ViewModelProvider(this,
            RestaurantViewModelFactory(RestaurantRepository(API_KEY))
        )[RestaurantViewModel::class.java]

        setupUi()
        observeData()

        // Ask location permission
        askLocationOrFallback()
    }

    @SuppressLint("MissingPermission")
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) getCurrentLocationAndRefresh()
        else viewModel.refreshListItems() // if someone refresh page than fallback to NYC
    }

    @SuppressLint("SetTextI18n")
    private fun setupUi() {
        findNearbyRestaurantsAdapter = FindNearbyRestaurantsAdapter()
        binding.rvRestaurant.layoutManager = LinearLayoutManager(this)
        binding.rvRestaurant.adapter = findNearbyRestaurantsAdapter

        // SeekBar range form 100–5000
        binding.seekRadius.max = 5000
        binding.seekRadius.progress = 500 // Default progress values set on 0.5 km
        binding.tvSelectedRadiusInKM.text = "0.5 KM"
        binding.seekRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                val r = value.coerceAtLeast(100)
                // Convert radius from int to double first
                val convertToDouble = r.toDouble()
                // Convert meters to kilometers for display
                val radiusInKilometers = convertToDouble / 1000.0
                binding.tvSelectedRadiusInKM.text = "$radiusInKilometers KM"
                viewModel.setRadius(r)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {
                // reload new data with new radius
                viewModel.refreshListItems()
            }
        })

        binding.swipe.setOnRefreshListener {
            viewModel.refreshListItems()
        }

        binding.rvRestaurant.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                LogUtils.showLog("Item COUNT", "onScrolled: $totalItemCount")
                LogUtils.showLog("LAST Visible Item", "onScrolled: $lastVisibleItem")

                val state = viewModel.state.value
                val isLoading = state?.isLoading ?: false
                val totalAvailable = state?.total ?: Int.MAX_VALUE
                LogUtils.showLog("Total Available Items", "onScrolled: $totalAvailable")

                // Load more when scrolled near bottom & not loading already
                if (!isLoading && lastVisibleItem >= totalItemCount - 1 && totalItemCount < totalAvailable) {
                    viewModel.loadMoreListItems()
                }
            }
        })
    }

    private fun observeData() {
        viewModel.state.observe(this) { dataList ->
            LogUtils.showLog("List items", "observe: "+dataList.items)
            // Top Loader
            if (dataList.isLoading) {
                binding.progressCenter.visibility = View.VISIBLE
                loadCustomLoader(binding.progressCenter)
            } else {
                binding.progressCenter.visibility = View.GONE
            }

            // Bottom Loader
            if (dataList.isLoadingMore) {
                binding.progressBottom.visibility = View.VISIBLE
                loadCustomLoader(binding.progressBottom)
            } else {
                binding.progressBottom.visibility = View.GONE
            }

            binding.swipe.isRefreshing = dataList.isLoading && binding.swipe.isRefreshing

            // Delay the update so it run after scroll is finished
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    findNearbyRestaurantsAdapter.updateData(dataList.items)
                }
            }

            dataList.error?.let {
                // this error toast
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Used for show custom loader with Coil Gif library
    private fun loadCustomLoader(imageView: ImageView) {
        imageView.load(R.drawable.loader) {
            crossfade(true)
            decoderFactory(
                if (Build.VERSION.SDK_INT >= 28) {
                    ImageDecoderDecoder.Factory()
                } else {
                    GifDecoder.Factory()
                }
            )
        }
    }

    private fun askLocationOrFallback() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndRefresh()
        } else {
            requestPermission.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getCurrentLocationAndRefresh() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                viewModel.setLocation(loc.latitude, loc.longitude, useCoords = true)
            } else {
                viewModel.setLocation(null, null, useCoords = false) // fallback to NYC
            }
            viewModel.refreshListItems()
        }.addOnFailureListener {
            viewModel.setLocation(null, null, useCoords = false) // fallback to NYC
            viewModel.refreshListItems()
        }
    }
}