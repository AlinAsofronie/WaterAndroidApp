package com.example.assetregisterapp.sync

import com.example.assetregisterapp.data.Asset
import com.example.assetregisterapp.data.AssetComplete
import com.example.assetregisterapp.data.SyncStatus
import com.example.assetregisterapp.data.toAsset
import com.example.assetregisterapp.data.toAssetComplete
import com.example.assetregisterapp.repository.AssetRepository
import com.example.assetregisterapp.repository.SupabaseAssetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class AssetSyncManager(
    private val localRepository: AssetRepository,
    private val remoteRepository: SupabaseAssetRepository
) {
    
    suspend fun syncAllAssets(): SyncResult = withContext(Dispatchers.IO) {
        try {
            // Get all local assets that need syncing
            val localAssets = getAllLocalAssets()
            val pendingAssets = localAssets.filter { it.syncStatus == SyncStatus.PENDING }
            
            // Get all remote assets
            val remoteAssets = remoteRepository.getAllAssets()
            
            var successCount = 0
            var errorCount = 0
            val errors = mutableListOf<String>()
            
            // Sync pending local assets to remote
            for (asset in pendingAssets) {
                try {
                    val result = if (asset.id == 0L) {
                        // New asset - insert to remote
                        remoteRepository.insertAsset(asset)
                    } else {
                        // Existing asset - update remote
                        remoteRepository.updateAsset(asset)
                    }
                    
                    if (result != null) {
                        // Update local asset with synced status
                        localRepository.update(asset.copy(
                            syncStatus = SyncStatus.SYNCED,
                            updatedAt = Date()
                        ))
                        successCount++
                    } else {
                        errorCount++
                        errors.add("Failed to sync asset: ${asset.name}")
                    }
                } catch (e: Exception) {
                    errorCount++
                    errors.add("Error syncing asset ${asset.name}: ${e.message}")
                }
            }
            
            // Download new assets from remote that don't exist locally
            for (remoteAsset in remoteAssets) {
                val localAsset = localAssets.find { it.id == remoteAsset.id }
                if (localAsset == null) {
                    try {
                        localRepository.insert(remoteAsset.copy(syncStatus = SyncStatus.SYNCED))
                        successCount++
                    } catch (e: Exception) {
                        errorCount++
                        errors.add("Error downloading asset ${remoteAsset.name}: ${e.message}")
                    }
                }
            }
            
            SyncResult(
                success = errorCount == 0,
                syncedCount = successCount,
                errorCount = errorCount,
                errors = errors
            )
            
        } catch (e: Exception) {
            SyncResult(
                success = false,
                syncedCount = 0,
                errorCount = 1,
                errors = listOf("Sync failed: ${e.message}")
            )
        }
    }
    
    suspend fun syncSingleAsset(asset: Asset): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = if (asset.id == 0L) {
                remoteRepository.insertAsset(asset)
            } else {
                remoteRepository.updateAsset(asset)
            }
            
            if (result != null) {
                localRepository.update(asset.copy(
                    syncStatus = SyncStatus.SYNCED,
                    updatedAt = Date()
                ))
                true
            } else {
                localRepository.update(asset.copy(
                    syncStatus = SyncStatus.ERROR,
                    updatedAt = Date()
                ))
                false
            }
        } catch (e: Exception) {
            localRepository.update(asset.copy(
                syncStatus = SyncStatus.ERROR,
                updatedAt = Date()
            ))
            false
        }
    }
    
    suspend fun downloadAllAssets(): List<Asset> = withContext(Dispatchers.IO) {
        try {
            remoteRepository.getAllAssets()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun uploadLocalAssets(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val localAssets = getAllLocalAssets()
            val pendingAssets = localAssets.filter { it.syncStatus == SyncStatus.PENDING }
            
            var successCount = 0
            var errorCount = 0
            val errors = mutableListOf<String>()
            
            for (asset in pendingAssets) {
                try {
                    val result = remoteRepository.insertAsset(asset)
                    if (result != null) {
                        localRepository.update(asset.copy(
                            syncStatus = SyncStatus.SYNCED,
                            updatedAt = Date()
                        ))
                        successCount++
                    } else {
                        errorCount++
                        errors.add("Failed to upload asset: ${asset.name}")
                    }
                } catch (e: Exception) {
                    errorCount++
                    errors.add("Error uploading asset ${asset.name}: ${e.message}")
                }
            }
            
            SyncResult(
                success = errorCount == 0,
                syncedCount = successCount,
                errorCount = errorCount,
                errors = errors
            )
        } catch (e: Exception) {
            SyncResult(
                success = false,
                syncedCount = 0,
                errorCount = 1,
                errors = listOf("Upload failed: ${e.message}")
            )
        }
    }
    
    private suspend fun getAllLocalAssets(): List<Asset> {
        // This would need to be implemented to get all assets from local repository
        // For now, returning empty list - would need to modify AssetRepository to support this
        return emptyList()
    }
}

data class SyncResult(
    val success: Boolean,
    val syncedCount: Int,
    val errorCount: Int,
    val errors: List<String>
)