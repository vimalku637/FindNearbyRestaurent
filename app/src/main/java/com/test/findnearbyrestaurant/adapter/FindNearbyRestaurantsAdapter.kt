package com.test.findnearbyrestaurant.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.test.findnearbyrestaurant.R
import com.test.findnearbyrestaurant.data.Business
import com.test.findnearbyrestaurant.databinding.ItemsFindNearbyRestaurantsBinding
import java.util.Locale

class FindNearbyRestaurantsAdapter : RecyclerView.Adapter<FindNearbyRestaurantsAdapter.ViewHolder>() {
    private val dataList = mutableListOf<Business>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(items: List<Business>) {
        dataList.clear()
        dataList.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemsFindNearbyRestaurantsBinding.inflate(LayoutInflater.from(p.context), p, false)
        return ViewHolder(itemBinding)
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) = holder.bind(dataList[pos])

    class ViewHolder(
        private val itemBinding: ItemsFindNearbyRestaurantsBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(b: Business) {
            // load image using Coil library
            itemBinding.img.load(b.image_url) {
                placeholder(R.drawable.bg_place_holder)
                error(R.drawable.bg_place_holder)
                crossfade(true)
            }
            itemBinding.tvName.text = b.name
            // Use this for show distance in meters & this can be change according to requirement
            val distanceInMeters = b.distance
            val city = b.location?.city ?: "Unknown"
            val state = b.location?.state ?: "Unknown"
            itemBinding.tvDistance.text =
                String.format(Locale.US, "%.2fm", distanceInMeters)+", "+
                        city+", "+
                        state

            // Check status of OPEN or CLOSED
            if (b.isClosed) {
                itemBinding.tvStatus.text = "Currently CLOSED"
            } else {
                itemBinding.tvStatus.text = "Currently OPEN"
            }
            itemBinding.tvRating.text = ""+b.rating
        }
    }
}