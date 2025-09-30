package com.example.assetregisterapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.assetregisterapp.R
import com.example.assetregisterapp.data.Asset
import com.example.assetregisterapp.data.toAssetComplete
import com.example.assetregisterapp.data.AssetStatus
import com.example.assetregisterapp.data.SyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssetAdapter(
    private val onItemClicked: (Asset) -> Unit,
    private val onEditClicked: (Asset) -> Unit
) :
    ListAdapter<Asset, AssetAdapter.AssetViewHolder>(AssetDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asset_modern, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val primaryIdentifierTextView: TextView = itemView.findViewById(R.id.asset_primary_identifier)
        private val assetTypeTextView: TextView = itemView.findViewById(R.id.asset_type)
        private val locationTextView: TextView = itemView.findViewById(R.id.location_text)
        private val barcodeTextView: TextView = itemView.findViewById(R.id.asset_barcode)
        private val currentValueTextView: TextView = itemView.findViewById(R.id.current_value)
        private val purchasePriceTextView: TextView = itemView.findViewById(R.id.purchase_price)
        private val syncStatusTextView: TextView = itemView.findViewById(R.id.sync_status)
        private val createdInfoTextView: TextView = itemView.findViewById(R.id.created_info)
        private val updatedInfoTextView: TextView = itemView.findViewById(R.id.updated_info)
        private val statusBadge: TextView = itemView.findViewById(R.id.status_badge)
        private val filterIndicator: TextView = itemView.findViewById(R.id.filter_indicator)
        private val editButton: MaterialButton = itemView.findViewById(R.id.button_edit)

        fun bind(asset: Asset) {
            // Convert Asset to AssetComplete to get full data structure
            val completeAsset = asset.toAssetComplete()
            
            // Primary identifier (name or asset ID)
            primaryIdentifierTextView.text = if (asset.name.isNotBlank()) {
                asset.name
            } else {
                completeAsset.primaryIdentifier.ifBlank { "Asset #${asset.id}" }
            }
            
            // Asset type/category
            assetTypeTextView.text = asset.category.ifBlank { "General Equipment" }
            
            // Location (wing + room)
            val locationText = buildString {
                if (completeAsset.wing?.isNotBlank() == true) {
                    append(completeAsset.wing)
                }
                if (completeAsset.room?.isNotBlank() == true) {
                    if (isNotEmpty()) append(" • ")
                    append(completeAsset.room)
                }
                if (isEmpty()) {
                    append(asset.location.ifBlank { "Location not set" })
                }
            }
            locationTextView.text = locationText
            
            // Barcode
            barcodeTextView.text = completeAsset.assetBarcode?.ifBlank { "No barcode" } ?: "No barcode"
            
            // Purchase Price
            purchasePriceTextView.text = if (asset.purchasePrice > 0) {
                String.format("$%.2f", asset.purchasePrice)
            } else {
                "Not set"
            }
            
            // Current Value
            currentValueTextView.text = if (asset.currentValue > 0) {
                String.format("$%.2f", asset.currentValue)
            } else {
                "Not set"
            }
            
            // Sync Status with color coding
            when (asset.syncStatus) {
                SyncStatus.PENDING -> {
                    syncStatusTextView.text = "Pending"
                    syncStatusTextView.setTextColor(itemView.context.getColor(R.color.sync_pending))
                }
                SyncStatus.SYNCED -> {
                    syncStatusTextView.text = "Synced"
                    syncStatusTextView.setTextColor(itemView.context.getColor(R.color.sync_synced))
                }
                SyncStatus.ERROR -> {
                    syncStatusTextView.text = "Error"
                    syncStatusTextView.setTextColor(itemView.context.getColor(R.color.sync_error))
                }
            }
            
            // Created info (date + user)
            val createdText = buildString {
                if (asset.createdAt != null) {
                    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                    append(formatter.format(asset.createdAt))
                }
                if (completeAsset.createdBy?.isNotBlank() == true) {
                    if (isNotEmpty()) append(" by ")
                    append(completeAsset.createdBy)
                }
                if (isEmpty()) append("No data")
            }
            createdInfoTextView.text = createdText
            
            // Updated info (date + user)
            val updatedText = buildString {
                if (asset.updatedAt != null) {
                    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                    append(formatter.format(asset.updatedAt))
                }
                if (completeAsset.updatedBy?.isNotBlank() == true) {
                    if (isNotEmpty()) append(" by ")
                    append(completeAsset.updatedBy)
                }
                if (isEmpty()) append("No updates")
            }
            updatedInfoTextView.text = updatedText
            
            // Status badge with color
            statusBadge.text = asset.status.name
            val statusColor = when (asset.status) {
                AssetStatus.ACTIVE -> R.color.status_active
                AssetStatus.MAINTENANCE -> R.color.status_maintenance
                AssetStatus.RETIRED -> R.color.status_retired
                AssetStatus.DISPOSED -> R.color.status_disposed
            }
            statusBadge.setBackgroundColor(itemView.context.getColor(statusColor))
            
            // Filter indicator (show if filter is due)
            val filterExpiryDate = completeAsset.filterExpiryDate
            val currentDate = Date()
            if (filterExpiryDate != null && filterExpiryDate.before(currentDate) && completeAsset.filterNeeded) {
                filterIndicator.visibility = View.VISIBLE
                filterIndicator.text = "FILTER DUE"
                filterIndicator.setBackgroundColor(itemView.context.getColor(R.color.apple_red))
            } else {
                filterIndicator.visibility = View.GONE
            }
            
            // Click listeners
            itemView.setOnClickListener {
                onItemClicked(asset)
            }
            
            editButton.setOnClickListener {
                onEditClicked(asset)
            }
        }
    }

    companion object {
        private val AssetDiffCallback = object : DiffUtil.ItemCallback<Asset>() {
            override fun areItemsTheSame(oldItem: Asset, newItem: Asset): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Asset, newItem: Asset): Boolean {
                return oldItem == newItem
            }
        }
    }
}