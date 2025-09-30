package com.example.assetregisterapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.assetregisterapp.data.Asset
import com.example.assetregisterapp.data.AssetDatabase
import com.example.assetregisterapp.data.AssetStatus
import com.example.assetregisterapp.repository.AssetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AssetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AssetRepository
    val allAssets: LiveData<List<Asset>>

    init {
        val assetDao = AssetDatabase.getDatabase(application).assetDao()
        repository = AssetRepository(assetDao)
        allAssets = repository.allAssets
    }

    fun insert(asset: Asset) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(asset)
    }

    fun update(asset: Asset) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(asset)
    }

    fun delete(asset: Asset) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(asset)
    }

    suspend fun getAssetById(id: Long): Asset? {
        return repository.getAssetById(id)
    }

    fun getAssetsByCategory(category: String): LiveData<List<Asset>> {
        return repository.getAssetsByCategory(category)
    }

    fun getAssetsByStatus(status: AssetStatus): LiveData<List<Asset>> {
        return repository.getAssetsByStatus(status)
    }

    suspend fun syncPendingAssets(): Int {
        return repository.syncPendingAssets()
    }
}