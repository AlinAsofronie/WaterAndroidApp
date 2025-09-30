package com.example.assetregisterapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assetregisterapp.adapter.AssetSchemaAdapter
import com.example.assetregisterapp.databinding.FragmentFirstBinding
import com.example.assetregisterapp.viewmodel.AssetViewModel

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    
    private val assetViewModel: AssetViewModel by viewModels()
    private lateinit var assetAdapter: AssetSchemaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeAssets()

        binding.fabAddAsset.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.fabImportAssets.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_ImportFragment)
        }

        binding.fabSyncAssets.setOnClickListener {
            syncAssetsToSupabase()
        }
    }

    private fun setupRecyclerView() {
        assetAdapter = AssetSchemaAdapter(
            onItemClicked = { asset ->
                // Handle asset item click - could navigate to detail view
            },
            onEditClicked = { asset ->
                // Navigate to edit screen with asset data
                val bundle = Bundle()
                bundle.putLong("assetId", asset.id)
                findNavController().navigate(R.id.action_FirstFragment_to_EditFragment, bundle)
            }
        )
        
        binding.recyclerViewAssets.apply {
            adapter = assetAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeAssets() {
        try {
            assetViewModel.allAssets.observe(viewLifecycleOwner) { assets ->
                val assetList = assets ?: emptyList()
                assetAdapter.submitList(assetList)
                
                // Show/hide empty state
                if (assetList.isEmpty()) {
                    binding.recyclerViewAssets.visibility = View.GONE
                    binding.emptyState.root.visibility = View.VISIBLE
                    
                    // Handle empty state button click
                    binding.emptyState.root.findViewById<MaterialButton>(R.id.button_add_first_asset)?.setOnClickListener {
                        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                    }
                } else {
                    binding.recyclerViewAssets.visibility = View.VISIBLE
                    binding.emptyState.root.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Show empty state if there's an error
            assetAdapter.submitList(emptyList())
            binding.recyclerViewAssets.visibility = View.GONE
            binding.emptyState.root.visibility = View.VISIBLE
        }
    }

    private fun syncAssetsToSupabase() {
        lifecycleScope.launch {
            try {
                // Show loading state
                binding.fabSyncAssets.isEnabled = false
                Toast.makeText(context, "Syncing assets to Supabase...", Toast.LENGTH_SHORT).show()
                
                // Perform sync
                val syncedCount = withContext(Dispatchers.IO) {
                    assetViewModel.syncPendingAssets()
                }
                
                // Show result
                val message = if (syncedCount > 0) {
                    "Successfully synced $syncedCount assets to Supabase!"
                } else {
                    "No pending assets to sync."
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Toast.makeText(context, "Sync failed: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } finally {
                binding.fabSyncAssets.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}