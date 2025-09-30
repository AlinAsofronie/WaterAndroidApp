package com.example.assetregisterapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY name ASC")
    fun getAllAssets(): LiveData<List<Asset>>

    @Query("SELECT * FROM assets WHERE id = :id")
    fun getAssetById(id: Long): Asset?

    @Query("SELECT * FROM assets WHERE category = :category ORDER BY name ASC")
    fun getAssetsByCategory(category: String): LiveData<List<Asset>>

    @Query("SELECT * FROM assets WHERE status = :status ORDER BY name ASC")
    fun getAssetsByStatus(status: AssetStatus): LiveData<List<Asset>>

    @Query("SELECT * FROM assets WHERE syncStatus = :syncStatus ORDER BY name ASC")
    fun getAssetsBySyncStatus(syncStatus: SyncStatus): List<Asset>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(asset: Asset): Long

    @Update
    fun update(asset: Asset)

    @Delete
    fun delete(asset: Asset)

    @Query("DELETE FROM assets")
    fun deleteAll()
}