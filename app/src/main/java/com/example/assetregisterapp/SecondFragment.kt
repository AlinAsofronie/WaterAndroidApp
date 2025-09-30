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

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    
    private val assetViewModel: AssetViewModel by viewModels()

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
    }


    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            saveAsset()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }


    private fun saveAsset() {
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

        val asset = Asset(
            name = primaryIdentifier,
            description = "",
            category = assetType,
            serialNumber = assetBarcode.ifEmpty { "N/A" },
            purchaseDate = Date(),
            purchasePrice = 0.0,
            currentValue = 0.0,
            location = if (wing.isNotEmpty() && room.isNotEmpty()) "$wing - $room" else wing.ifEmpty { room.ifEmpty { "Unknown" } },
            status = AssetStatus.ACTIVE
        )

        assetViewModel.insert(asset)
        Toast.makeText(context, "Asset saved successfully", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}