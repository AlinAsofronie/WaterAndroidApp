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

class AssetSchemaAdapter(
    private val onItemClicked: (Asset) -> Unit,
    private val onEditClicked: (Asset) -> Unit
) : ListAdapter<Asset, AssetSchemaAdapter.AssetViewHolder>(AssetDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asset_schema, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Schema fields from Supabase
        private val primaryIdentifierTextView: TextView = itemView.findViewById(R.id.primary_identifier)
        private val assetTypeTextView: TextView = itemView.findViewById(R.id.asset_type)
        private val statusBadge: TextView = itemView.findViewById(R.id.status_badge)
        private val locationTextView: TextView = itemView.findViewById(R.id.location_text)
        private val assetBarcodeTextView: TextView = itemView.findViewById(R.id.asset_barcode)
        private val secondaryIdentifierTextView: TextView = itemView.findViewById(R.id.secondary_identifier)
        private val filterTypeTextView: TextView = itemView.findViewById(R.id.filter_type)
        private val filterStatusTextView: TextView = itemView.findViewById(R.id.filter_status)
        private val augmentedCareBadge: TextView = itemView.findViewById(R.id.augmented_care_badge)
        private val lowUsageBadge: TextView = itemView.findViewById(R.id.low_usage_badge)
        private val needsFlushingBadge: TextView = itemView.findViewById(R.id.needs_flushing_badge)
        private val syncStatusTextView: TextView = itemView.findViewById(R.id.sync_status)
        private val editButton: MaterialButton = itemView.findViewById(R.id.button_edit)

        fun bind(asset: Asset) {
            // Convert to complete asset to access all schema fields
            val completeAsset = asset.toAssetComplete()
            
            // Primary Identifier (main identifier)
            primaryIdentifierTextView.text = completeAsset.primaryIdentifier.ifBlank { 
                "Asset #${asset.id}" 
            }
            
            // Asset Type
            assetTypeTextView.text = completeAsset.assetType.ifBlank { "Unknown Type" }
            
            // Status Badge with color
            statusBadge.text = asset.status.name.uppercase()
            val statusColor = when (asset.status) {
                AssetStatus.ACTIVE -> R.color.status_active
                AssetStatus.MAINTENANCE -> R.color.status_maintenance
                AssetStatus.RETIRED -> R.color.status_retired
                AssetStatus.DISPOSED -> R.color.status_disposed
            }
            statusBadge.setBackgroundColor(itemView.context.getColor(statusColor))
            
            // Location (wing + room combination)
            val locationText = buildString {
                if (completeAsset.wing?.isNotBlank() == true) {
                    append(completeAsset.wing)
                }
                if (completeAsset.room?.isNotBlank() == true) {
                    if (isNotEmpty()) append(" • Room ")
                    else append("Room ")
                    append(completeAsset.room)
                }
                if (completeAsset.roomName?.isNotBlank() == true && completeAsset.roomName != completeAsset.room) {
                    append(" (${completeAsset.roomName})")
                }
                if (isEmpty()) {
                    append("Location not set")
                }
            }
            locationTextView.text = locationText
            
            // Asset Barcode
            assetBarcodeTextView.text = completeAsset.assetBarcode?.ifBlank { "No barcode" } 
                ?: "No barcode"
            
            // Secondary Identifier
            secondaryIdentifierTextView.text = completeAsset.secondaryIdentifier?.ifBlank { "None" } 
                ?: "None"
            
            // Filter Type
            filterTypeTextView.text = completeAsset.filterType?.ifBlank { "N/A" } ?: "N/A"
            
            // Filter Status
            val filterStatusText = when {
                !completeAsset.filterNeeded -> "Not needed"
                completeAsset.filtersOn -> "Active"
                else -> "Inactive"
            }
            filterStatusTextView.text = filterStatusText
            
            // Maintenance Badges
            augmentedCareBadge.visibility = if (completeAsset.augmentedCare) {
                augmentedCareBadge.setBackgroundColor(itemView.context.getColor(R.color.apple_purple))
                View.VISIBLE
            } else View.GONE
            
            lowUsageBadge.visibility = if (completeAsset.lowUsageAsset) {
                lowUsageBadge.setBackgroundColor(itemView.context.getColor(R.color.apple_gray))
                View.VISIBLE
            } else View.GONE
            
            needsFlushingBadge.visibility = if (completeAsset.needsFlushing) {
                needsFlushingBadge.setBackgroundColor(itemView.context.getColor(R.color.apple_orange))
                View.VISIBLE
            } else View.GONE
            
            // Sync Status with color coding
            val syncText = when (asset.syncStatus) {
                SyncStatus.PENDING -> "• Pending sync"
                SyncStatus.SYNCED -> "• Synced"
                SyncStatus.ERROR -> "• Sync error"
            }
            syncStatusTextView.text = syncText
            
            val syncColor = when (asset.syncStatus) {
                SyncStatus.PENDING -> R.color.sync_pending
                SyncStatus.SYNCED -> R.color.sync_synced
                SyncStatus.ERROR -> R.color.sync_error
            }
            syncStatusTextView.setTextColor(itemView.context.getColor(syncColor))
            
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