package com.example.assetregisterapp.repository

import androidx.lifecycle.LiveData
import com.example.assetregisterapp.data.Asset
import com.example.assetregisterapp.data.AssetDao
import com.example.assetregisterapp.data.AssetStatus
import com.example.assetregisterapp.data.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class AssetRepository(private val assetDao: AssetDao) {

    private val supabaseRepository = SupabaseAssetRepository()
    val allAssets: LiveData<List<Asset>> = assetDao.getAllAssets()

    suspend fun insert(asset: Asset): Long {
        return withContext(Dispatchers.IO) {
            // Add timestamps and set status
            val assetWithTimestamp = asset.copy(
                createdAt = Date(),
                updatedAt = Date(),
                syncStatus = SyncStatus.PENDING
            )
            
            // Insert locally first
            val localId = assetDao.insert(assetWithTimestamp)
            val finalAsset = assetWithTimestamp.copy(id = localId)
            
            // Try to sync to Supabase
            try {
                val remoteAsset = supabaseRepository.insertAsset(finalAsset)
                if (remoteAsset != null) {
                    // Update local with synced status
                    assetDao.update(finalAsset.copy(syncStatus = SyncStatus.SYNCED))
                }
            } catch (e: Exception) {
                // Mark as error if sync failed
                assetDao.update(finalAsset.copy(syncStatus = SyncStatus.ERROR))
                e.printStackTrace()
            }
            
            localId
        }
    }

    suspend fun update(asset: Asset) {
        withContext(Dispatchers.IO) {
            // Update timestamp and set pending sync
            val updatedAsset = asset.copy(
                updatedAt = Date(),
                syncStatus = SyncStatus.PENDING
            )
            
            // Update locally first
            assetDao.update(updatedAsset)
            
            // Try to sync to Supabase
            try {
                val remoteAsset = supabaseRepository.updateAsset(updatedAsset)
                if (remoteAsset != null) {
                    // Update local with synced status
                    assetDao.update(updatedAsset.copy(syncStatus = SyncStatus.SYNCED))
                }
            } catch (e: Exception) {
                // Mark as error if sync failed
                assetDao.update(updatedAsset.copy(syncStatus = SyncStatus.ERROR))
                e.printStackTrace()
            }
        }
    }

    suspend fun delete(asset: Asset) {
        withContext(Dispatchers.IO) {
            assetDao.delete(asset)
        }
    }

    suspend fun getAssetById(id: Long): Asset? {
        return withContext(Dispatchers.IO) {
            assetDao.getAssetById(id)
        }
    }

    fun getAssetsByCategory(category: String): LiveData<List<Asset>> {
        return assetDao.getAssetsByCategory(category)
    }

    fun getAssetsByStatus(status: AssetStatus): LiveData<List<Asset>> {
        return assetDao.getAssetsByStatus(status)
    }
    
    // Manual sync function for pending assets
    suspend fun syncPendingAssets(): Int {
        return withContext(Dispatchers.IO) {
            val pendingAssets = assetDao.getAssetsBySyncStatus(SyncStatus.PENDING)
            var synced = 0
            
            for (asset in pendingAssets) {
                try {
                    val remoteAsset = supabaseRepository.insertAsset(asset)
                    if (remoteAsset != null) {
                        assetDao.update(asset.copy(syncStatus = SyncStatus.SYNCED))
                        synced++
                    }
                } catch (e: Exception) {
                    assetDao.update(asset.copy(syncStatus = SyncStatus.ERROR))
                    e.printStackTrace()
                }
            }
            synced
        }
    }
}