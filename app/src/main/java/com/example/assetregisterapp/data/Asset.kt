package com.example.assetregisterapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Entity(tableName = "assets")
@Serializable
data class Asset(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Long = 0,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("category")
    val category: String,
    
    @SerialName("serial_number")
    val serialNumber: String,
    
    @SerialName("purchase_date")
    val purchaseDate: @Serializable(with = DateSerializer::class) Date,
    
    @SerialName("purchase_price")
    val purchasePrice: Double,
    
    @SerialName("current_value")
    val currentValue: Double,
    
    @SerialName("location")
    val location: String,
    
    @SerialName("status")
    val status: AssetStatus = AssetStatus.ACTIVE,
    
    @SerialName("created_at")
    val createdAt: @Serializable(with = DateSerializer::class) Date? = null,
    
    @SerialName("updated_at")
    val updatedAt: @Serializable(with = DateSerializer::class) Date? = null,
    
    @SerialName("sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

@Serializable
enum class AssetStatus {
    @SerialName("active")
    ACTIVE,
    @SerialName("maintenance")
    MAINTENANCE,
    @SerialName("retired")
    RETIRED,
    @SerialName("disposed")
    DISPOSED
}

@Serializable
enum class SyncStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("synced")
    SYNCED,
    @SerialName("error")
    ERROR
}