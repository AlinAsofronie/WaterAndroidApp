package com.example.assetregisterapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.assetregisterapp.data.Asset
import com.example.assetregisterapp.data.AssetStatus
import com.example.assetregisterapp.databinding.FragmentSecondBinding
import com.example.assetregisterapp.viewmodel.AssetViewModel
import java.util.*

class EditFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    
    private val assetViewModel: AssetViewModel by viewModels()
    private var assetId: Long = 0L
    private var currentAsset: Asset? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        loadAsset()
    }

    private fun loadAsset() {
        // Get asset ID from arguments
        assetId = arguments?.getLong("assetId", 0L) ?: 0L
        
        assetViewModel.allAssets.observe(viewLifecycleOwner) { assets ->
            currentAsset = assets?.find { it.id == assetId }
            currentAsset?.let { asset ->
                populateFields(asset)
            }
        }
    }

    private fun populateFields(asset: Asset) {
        binding.editPrimaryIdentifier.setText(asset.name)
        binding.editAssetType.setText(asset.category)
        binding.editAssetBarcode.setText(asset.serialNumber)
        binding.editSecondaryIdentifier.setText("")
        
        // Parse location into wing and room if formatted as "wing - room"
        val locationParts = asset.location.split(" - ")
        if (locationParts.size == 2) {
            binding.editWing.setText(locationParts[0])
            binding.editRoom.setText(locationParts[1])
        } else {
            binding.editWing.setText(asset.location)
            binding.editRoom.setText("")
        }
    }


    private fun setupButtons() {
        binding.buttonSave.text = "Update Asset"
        binding.buttonSave.setOnClickListener {
            updateAsset()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigate(R.id.action_EditFragment_to_FirstFragment)
        }
    }


    private fun updateAsset() {
        val primaryIdentifier = binding.editPrimaryIdentifier.text.toString().trim()
        val assetType = binding.editAssetType.text.toString().trim()
        val assetBarcode = binding.editAssetBarcode.text.toString().trim()
        val secondaryIdentifier = binding.editSecondaryIdentifier.text.toString().trim()
        val wing = binding.editWing.text.toString().trim()
        val room = binding.editRoom.text.toString().trim()

        if (primaryIdentifier.isEmpty() || assetType.isEmpty()) {
            Toast.makeText(context, "Please fill required fields: Primary Identifier and Asset Type", Toast.LENGTH_SHORT).show()
            return
        }

        currentAsset?.let { asset ->
            val updatedAsset = asset.copy(
                name = primaryIdentifier,
                description = "",
                category = assetType,
                serialNumber = assetBarcode.ifEmpty { "N/A" },
                purchaseDate = Date(),
                purchasePrice = 0.0,
                currentValue = 0.0,
                location = if (wing.isNotEmpty() && room.isNotEmpty()) "$wing - $room" else wing.ifEmpty { room.ifEmpty { "Unknown" } },
                updatedAt = Date()
            )

            assetViewModel.update(updatedAsset)
            Toast.makeText(context, "Asset updated successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_EditFragment_to_FirstFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}