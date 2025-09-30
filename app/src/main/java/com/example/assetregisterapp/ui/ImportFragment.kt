package com.example.assetregisterapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.assetregisterapp.databinding.FragmentImportBinding
import com.example.assetregisterapp.util.CsvReader
import com.example.assetregisterapp.util.ExcelImportHelper
import com.example.assetregisterapp.viewmodel.AssetViewModel
import com.example.assetregisterapp.data.toAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportFragment : Fragment() {
    
    private var _binding: FragmentImportBinding? = null
    private val binding get() = _binding!!
    
    private val assetViewModel: AssetViewModel by viewModels()
    
    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importCsvFile(uri)
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.buttonSelectFile.setOnClickListener {
            openFilePicker()
        }
        
        binding.buttonImportSample.setOnClickListener {
            importSampleData()
        }
        
        // Show expected CSV format
        binding.textCsvFormat.text = """
            Expected CSV Format:
            ${ExcelImportHelper.createCsvHeader()}
            
            Instructions:
            1. Export your Excel file as CSV
            2. Ensure first row contains headers
            3. Select the CSV file using the button above
        """.trimIndent()
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/*" // CSV files
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun importCsvFile(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonSelectFile.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    processCsvFile(uri)
                }
                
                binding.progressBar.visibility = View.GONE
                binding.buttonSelectFile.isEnabled = true
                
                if (result.success) {
                    Toast.makeText(
                        context,
                        "Successfully imported ${result.importedCount} assets",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    binding.textImportResult.text = """
                        Import Results:
                        ✅ ${result.importedCount} assets imported
                        ❌ ${result.errorCount} errors
                        
                        ${if (result.errors.isNotEmpty()) "Errors:\n${result.errors.joinToString("\n")}" else ""}
                    """.trimIndent()
                } else {
                    Toast.makeText(
                        context,
                        "Import failed: ${result.errors.firstOrNull() ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.buttonSelectFile.isEnabled = true
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private suspend fun processCsvFile(uri: Uri): ImportResult {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
            ?: return ImportResult(false, 0, 1, listOf("Could not open file"))
        
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines()
        
        if (lines.isEmpty()) {
            return ImportResult(false, 0, 1, listOf("File is empty"))
        }
        
        // Skip header row (first line)
        val dataLines = lines.drop(1)
        
        var importedCount = 0
        var errorCount = 0
        val errors = mutableListOf<String>()
        
        for ((index, line) in dataLines.withIndex()) {
            try {
                val rowData = CsvReader.parseCsvLine(line)
                val asset = ExcelImportHelper.parseExcelRow(rowData)
                
                if (asset != null) {
                    try {
                        // Convert to simplified Asset for existing ViewModel
                        assetViewModel.insert(asset.toAsset())
                        importedCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorCount++
                        errors.add("Row ${index + 2}: Error saving asset - ${e.message}")
                    }
                } else {
                    errorCount++
                    errors.add("Row ${index + 2}: Invalid data format")
                }
                
            } catch (e: Exception) {
                errorCount++
                errors.add("Row ${index + 2}: ${e.message}")
            }
        }
        
        return ImportResult(
            success = errorCount == 0,
            importedCount = importedCount,
            errorCount = errorCount,
            errors = errors
        )
    }
    
    private fun importSampleData() {
        lifecycleScope.launch {
            try {
                // Create sample data matching your Excel structure
                val sampleAssets = createSampleAssets()
                
                sampleAssets.forEach { asset ->
                    assetViewModel.insert(asset.toAsset())
                }
                
                Toast.makeText(
                    context,
                    "Imported ${sampleAssets.size} sample assets",
                    Toast.LENGTH_SHORT
                ).show()
                
                binding.textImportResult.text = """
                    Sample Data Imported:
                    ✅ ${sampleAssets.size} sample assets created
                    
                    These demonstrate the full Excel structure with:
                    - Asset barcodes and identifiers
                    - Location details (wing, room, floor)
                    - Filter management information
                    - Maintenance flags and care requirements
                """.trimIndent()
                
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating sample data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun createSampleAssets(): List<com.example.assetregisterapp.data.AssetComplete> {
        return listOf(
            com.example.assetregisterapp.data.AssetComplete(
                assetBarcode = "BC001",
                primaryIdentifier = "LAPTOP-DEV-001",
                secondaryIdentifier = "DEV-LAP-01",
                assetType = "Laptop Computer",
                status = com.example.assetregisterapp.data.AssetStatus.ACTIVE,
                wing = "North Wing",
                wingShort = "NW",
                room = "NW-101",
                floor = "1",
                floorWords = "First Floor",
                roomNumber = "101",
                roomName = "Development Office",
                filterNeeded = false,
                needsFlushing = false,
                augmentedCare = false,
                lowUsageAsset = false,
                name = "Development Laptop",
                category = "IT Equipment",
                location = "North Wing - NW-101 - Development Office"
            ),
            com.example.assetregisterapp.data.AssetComplete(
                assetBarcode = "BC002",
                primaryIdentifier = "FILTER-KITCHEN-001",
                secondaryIdentifier = "FLT-KIT-01",
                assetType = "Water Filter System",
                status = com.example.assetregisterapp.data.AssetStatus.ACTIVE,
                wing = "East Wing",
                wingShort = "EW",
                room = "EW-KITCHEN",
                floor = "1",
                floorWords = "Ground Floor",
                roomNumber = "KITCHEN",
                roomName = "Main Kitchen",
                filterNeeded = true,
                filtersOn = true,
                filterExpiryDate = java.util.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000), // 30 days from now
                filterType = "Carbon Block",
                needsFlushing = true,
                augmentedCare = true,
                lowUsageAsset = false,
                name = "Kitchen Water Filter",
                category = "Water Treatment",
                location = "East Wing - Kitchen - Main Kitchen"
            ),
            com.example.assetregisterapp.data.AssetComplete(
                assetBarcode = "BC003",
                primaryIdentifier = "CHAIR-CONF-001",
                secondaryIdentifier = "CHR-CONF-01",
                assetType = "Conference Chair",
                status = com.example.assetregisterapp.data.AssetStatus.ACTIVE,
                wing = "South Wing",
                wingShort = "SW",
                room = "SW-CONF",
                floor = "2",
                floorWords = "Second Floor",
                roomNumber = "CONF",
                roomName = "Main Conference Room",
                filterNeeded = false,
                needsFlushing = false,
                augmentedCare = false,
                lowUsageAsset = true,
                name = "Conference Room Chair",
                category = "Furniture",
                location = "South Wing - Conference - Main Conference Room"
            )
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class ImportResult(
    val success: Boolean,
    val importedCount: Int,
    val errorCount: Int,
    val errors: List<String>
)