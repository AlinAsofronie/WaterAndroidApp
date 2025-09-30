package com.example.assetregisterapp.repository

import com.example.assetregisterapp.config.SupabaseConfig
import com.example.assetregisterapp.data.Asset
import com.example.assetregisterapp.data.AssetComplete
import com.example.assetregisterapp.data.AssetStatus
import com.example.assetregisterapp.data.SyncStatus
import com.example.assetregisterapp.data.toAssetComplete
import com.example.assetregisterapp.data.toAsset
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SupabaseAssetRepository {
    private val supabase = SupabaseConfig.client

    suspend fun insertAsset(asset: Asset): Asset? {
        return try {
            val client = supabase ?: return null
            // Convert to AssetComplete which has all the proper Supabase schema fields
            val completeAsset = asset.toAssetComplete().copy(
                syncStatus = SyncStatus.SYNCED
            )
            
            val result = client.from("assets")
                .insert(completeAsset) {
                    select(Columns.ALL)
                }
                .decodeSingle<AssetComplete>()
            
            // Convert back to simplified Asset
            result?.toAsset()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateAsset(asset: Asset): Asset? {
        return try {
            val client = supabase ?: return null
            // Convert to AssetComplete which has all the proper Supabase schema fields
            val completeAsset = asset.toAssetComplete().copy(
                syncStatus = SyncStatus.SYNCED
            )
            
            val result = client.from("assets")
                .update(completeAsset) {
                    filter {
                        eq("id", asset.id)
                    }
                    select(Columns.ALL)
                }
                .decodeSingle<AssetComplete>()
                
            // Convert back to simplified Asset
            result?.toAsset()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteAsset(assetId: Long): Boolean {
        return try {
            val client = supabase ?: return false
            client.from("assets")
                .delete {
                    filter {
                        eq("id", assetId)
                    }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getAllAssets(): List<Asset> {
        return try {
            val client = supabase ?: return emptyList()
            val completeAssets = client.from("assets")
                .select()
                .decodeList<AssetComplete>()
            
            // Convert to simplified Assets
            completeAssets.map { it.toAsset() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAssetById(id: Long): Asset? {
        return try {
            val client = supabase ?: return null
            client.from("assets")
                .select() {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<Asset>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAssetsByCategory(category: String): List<Asset> {
        return try {
            val client = supabase ?: return emptyList()
            client.from("assets")
                .select() {
                    filter {
                        eq("category", category)
                    }
                }
                .decodeList<Asset>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAssetsByStatus(status: AssetStatus): List<Asset> {
        return try {
            val client = supabase ?: return emptyList()
            client.from("assets")
                .select() {
                    filter {
                        eq("status", status.name.lowercase())
                    }
                }
                .decodeList<Asset>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun subscribeToAssetChanges(): Flow<List<Asset>> = flow {
        // This would implement real-time subscriptions
        // For now, we'll emit the current list
        emit(getAllAssets())
    }
}