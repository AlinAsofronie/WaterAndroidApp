package com.example.assetregisterapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Entity(tableName = "assets")
@Serializable
data class AssetComplete(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Long = 0,
    
    // Basic Asset Information
    @SerialName("asset_barcode")
    val assetBarcode: String? = null,
    
    @SerialName("primary_identifier")
    val primaryIdentifier: String,
    
    @SerialName("secondary_identifier")
    val secondaryIdentifier: String? = null,
    
    @SerialName("asset_type")
    val assetType: String,
    
    @SerialName("status")
    val status: AssetStatus = AssetStatus.ACTIVE,
    
    // Location Information
    @SerialName("wing")
    val wing: String? = null,
    
    @SerialName("wing_short")
    val wingShort: String? = null,
    
    @SerialName("room")
    val room: String? = null,
    
    @SerialName("floor")
    val floor: String? = null,
    
    @SerialName("floor_words")
    val floorWords: String? = null,
    
    @SerialName("room_number")
    val roomNumber: String? = null,
    
    @SerialName("room_name")
    val roomName: String? = null,
    
    // Filter Information
    @SerialName("filter_needed")
    val filterNeeded: Boolean = false,
    
    @SerialName("filters_on")
    val filtersOn: Boolean = false,
    
    @SerialName("filter_installed_on")
    val filterInstalledOn: @Serializable(with = DateSerializer::class) Date? = null,
    
    @SerialName("filter_expiry_date")
    val filterExpiryDate: @Serializable(with = DateSerializer::class) Date? = null,
    
    @SerialName("filter_type")
    val filterType: String? = null,
    
    // Maintenance Information
    @SerialName("needs_flushing")
    val needsFlushing: Boolean = false,
    
    @SerialName("notes")
    val notes: String? = null,
    
    // Care and Usage Flags
    @SerialName("augmented_care")
    val augmentedCare: Boolean = false,
    
    @SerialName("low_usage_asset")
    val lowUsageAsset: Boolean = false,
    
    // Legacy fields for compatibility with existing app (only include if they exist in your schema)
    @SerialName("name")
    val name: String = "",
    
    @SerialName("description")
    val description: String = "",
    
    @SerialName("category")
    val category: String = "",
    
    @SerialName("serial_number")
    val serialNumber: String? = null,
    
    @SerialName("location")
    val location: String = "",
    
    // Sync and Tracking
    @SerialName("sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    
    // Timestamps
    @SerialName("created_at")
    val createdAt: @Serializable(with = DateSerializer::class) Date? = null,
    
    @SerialName("created_by")
    val createdBy: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: @Serializable(with = DateSerializer::class) Date? = null,
    
    @SerialName("updated_by")
    val updatedBy: String? = null
)

// Extension function to convert AssetComplete to simplified Asset for backward compatibility
fun AssetComplete.toAsset(): Asset = Asset(
    id = this.id,
    name = this.name.ifEmpty { this.primaryIdentifier },
    description = this.description.ifEmpty { this.assetType },
    category = this.category.ifEmpty { this.assetType },
    serialNumber = this.serialNumber ?: this.assetBarcode ?: "",
    purchaseDate = Date(), // Default date since this field doesn't exist in Supabase
    purchasePrice = 0.0, // Default value since this field doesn't exist in Supabase
    currentValue = 0.0, // Default value since this field doesn't exist in Supabase
    location = this.location.ifEmpty { "${this.wing ?: ""} ${this.room ?: ""}".trim() },
    status = this.status,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    syncStatus = this.syncStatus
)

// Extension function to convert simplified Asset to AssetComplete
fun Asset.toAssetComplete(): AssetComplete {
    // Parse location into wing and room if formatted as "wing - room"
    val locationParts = this.location.split(" - ")
    val wing = if (locationParts.size >= 1) locationParts[0].trim() else null
    val room = if (locationParts.size >= 2) locationParts[1].trim() else null
    
    return AssetComplete(
        id = this.id,
        // Map to proper Supabase schema fields
        primaryIdentifier = this.name,
        assetType = this.category,
        assetBarcode = this.serialNumber,
        secondaryIdentifier = null,
        status = this.status,
        wing = wing,
        room = room,
        // Legacy fields for compatibility (only include fields that exist in Supabase)
        name = this.name,
        description = this.description,
        category = this.category,
        serialNumber = this.serialNumber,
        location = this.location,
        syncStatus = this.syncStatus,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}