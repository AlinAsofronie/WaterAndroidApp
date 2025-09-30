package com.example.assetregisterapp.util

import com.example.assetregisterapp.data.AssetComplete
import com.example.assetregisterapp.data.AssetStatus
import com.example.assetregisterapp.data.SyncStatus
import java.text.SimpleDateFormat
import java.util.*

class ExcelImportHelper {
    
    companion object {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        // Column mapping based on your Excel structure
        private const val COL_ASSET_BARCODE = 0
        private const val COL_PRIMARY_IDENTIFIER = 1
        private const val COL_SECONDARY_IDENTIFIER = 2
        private const val COL_ASSET_TYPE = 3
        private const val COL_STATUS = 4
        private const val COL_WING = 5
        private const val COL_WING_SHORT = 6
        private const val COL_ROOM = 7
        private const val COL_FLOOR = 8
        private const val COL_FLOOR_WORDS = 9
        private const val COL_ROOM_NUMBER = 10
        private const val COL_ROOM_NAME = 11
        private const val COL_FILTER_NEEDED = 12
        private const val COL_FILTERS_ON = 13
        private const val COL_FILTER_INSTALLED_ON = 14
        private const val COL_FILTER_EXPIRY_DATE = 15
        private const val COL_FILTER_TYPE = 16
        private const val COL_NEEDS_FLUSHING = 17
        private const val COL_NOTES = 18
        private const val COL_AUGMENTED_CARE = 19
        private const val COL_LOW_USAGE_ASSET = 20
        private const val COL_CREATED = 21
        private const val COL_CREATED_BY = 22
        private const val COL_MODIFIED = 23
        private const val COL_MODIFIED_BY = 24
        
        fun parseExcelRow(rowData: List<String>): AssetComplete? {
            return try {
                if (rowData.size < 25 || rowData[COL_PRIMARY_IDENTIFIER].isBlank()) {
                    return null // Skip invalid rows
                }
                
                AssetComplete(
                    assetBarcode = rowData.getOrNull(COL_ASSET_BARCODE)?.takeIf { it.isNotBlank() },
                    primaryIdentifier = rowData[COL_PRIMARY_IDENTIFIER],
                    secondaryIdentifier = rowData.getOrNull(COL_SECONDARY_IDENTIFIER)?.takeIf { it.isNotBlank() },
                    assetType = rowData.getOrNull(COL_ASSET_TYPE) ?: "Unknown",
                    status = parseAssetStatus(rowData.getOrNull(COL_STATUS)),
                    wing = rowData.getOrNull(COL_WING)?.takeIf { it.isNotBlank() },
                    wingShort = rowData.getOrNull(COL_WING_SHORT)?.takeIf { it.isNotBlank() },
                    room = rowData.getOrNull(COL_ROOM)?.takeIf { it.isNotBlank() },
                    floor = rowData.getOrNull(COL_FLOOR)?.takeIf { it.isNotBlank() },
                    floorWords = rowData.getOrNull(COL_FLOOR_WORDS)?.takeIf { it.isNotBlank() },
                    roomNumber = rowData.getOrNull(COL_ROOM_NUMBER)?.takeIf { it.isNotBlank() },
                    roomName = rowData.getOrNull(COL_ROOM_NAME)?.takeIf { it.isNotBlank() },
                    filterNeeded = parseBoolean(rowData.getOrNull(COL_FILTER_NEEDED)),
                    filtersOn = parseBoolean(rowData.getOrNull(COL_FILTERS_ON)),
                    filterInstalledOn = parseDate(rowData.getOrNull(COL_FILTER_INSTALLED_ON)),
                    filterExpiryDate = parseDate(rowData.getOrNull(COL_FILTER_EXPIRY_DATE)),
                    filterType = rowData.getOrNull(COL_FILTER_TYPE)?.takeIf { it.isNotBlank() },
                    needsFlushing = parseBoolean(rowData.getOrNull(COL_NEEDS_FLUSHING)),
                    notes = rowData.getOrNull(COL_NOTES)?.takeIf { it.isNotBlank() },
                    augmentedCare = parseBoolean(rowData.getOrNull(COL_AUGMENTED_CARE)),
                    lowUsageAsset = parseBoolean(rowData.getOrNull(COL_LOW_USAGE_ASSET)),
                    createdAt = parseDate(rowData.getOrNull(COL_CREATED)),
                    createdBy = rowData.getOrNull(COL_CREATED_BY)?.takeIf { it.isNotBlank() },
                    updatedAt = parseDate(rowData.getOrNull(COL_MODIFIED)),
                    updatedBy = rowData.getOrNull(COL_MODIFIED_BY)?.takeIf { it.isNotBlank() },
                    
                    // Set backward compatibility fields
                    name = rowData[COL_PRIMARY_IDENTIFIER],
                    description = rowData.getOrNull(COL_NOTES) ?: "",
                    category = rowData.getOrNull(COL_ASSET_TYPE) ?: "Unknown",
                    serialNumber = rowData.getOrNull(COL_ASSET_BARCODE),
                    location = buildLocationString(
                        rowData.getOrNull(COL_WING),
                        rowData.getOrNull(COL_ROOM),
                        rowData.getOrNull(COL_ROOM_NAME)
                    ),
                    syncStatus = SyncStatus.PENDING
                )
            } catch (e: Exception) {
                null // Skip problematic rows
            }
        }
        
        private fun parseAssetStatus(status: String?): AssetStatus {
            return when (status?.lowercase()?.trim()) {
                "active", "in_use", "operational" -> AssetStatus.ACTIVE
                "maintenance", "repair", "servicing" -> AssetStatus.MAINTENANCE
                "retired", "end_of_life" -> AssetStatus.RETIRED
                "disposed", "scrapped" -> AssetStatus.DISPOSED
                else -> AssetStatus.ACTIVE
            }
        }
        
        private fun parseBoolean(value: String?): Boolean {
            return when (value?.lowercase()?.trim()) {
                "true", "yes", "y", "1", "on" -> true
                else -> false
            }
        }
        
        private fun parseDate(dateString: String?): Date? {
            if (dateString.isNullOrBlank()) return null
            
            return try {
                // Try different date formats
                val formats = listOf(
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                    SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                )
                
                formats.firstNotNullOfOrNull { format ->
                    try {
                        format.parse(dateString)
                    } catch (e: Exception) {
                        null
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
        
        private fun buildLocationString(wing: String?, room: String?, roomName: String?): String {
            return listOfNotNull(wing, room, roomName)
                .filter { it.isNotBlank() }
                .joinToString(" - ")
        }
        
        // Helper function to create CSV header for export
        fun createCsvHeader(): String {
            return listOf(
                "Asset Barcode",
                "Primary Identifier", 
                "Secondary Identifier",
                "Asset Type",
                "Status",
                "Wing",
                "Wing (Short)",
                "Room",
                "Floor", 
                "Floor (Words)",
                "Room Number",
                "Room Name",
                "Filter Needed",
                "Filters On",
                "Filter Installed On",
                "Filter Expiry Date",
                "Filter Type",
                "Needs Flushing",
                "Notes",
                "Augmented Care",
                "Low Usage Asset",
                "Created",
                "Created By",
                "Modified",
                "Modified By"
            ).joinToString(",")
        }
        
        // Helper function to convert AssetComplete to CSV row
        fun assetToCsvRow(asset: AssetComplete): String {
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            return listOf(
                asset.assetBarcode ?: "",
                asset.primaryIdentifier,
                asset.secondaryIdentifier ?: "",
                asset.assetType,
                asset.status.name.lowercase(),
                asset.wing ?: "",
                asset.wingShort ?: "",
                asset.room ?: "",
                asset.floor ?: "",
                asset.floorWords ?: "",
                asset.roomNumber ?: "",
                asset.roomName ?: "",
                if (asset.filterNeeded) "Yes" else "No",
                if (asset.filtersOn) "Yes" else "No",
                asset.filterInstalledOn?.let { dateFormatter.format(it) } ?: "",
                asset.filterExpiryDate?.let { dateFormatter.format(it) } ?: "",
                asset.filterType ?: "",
                if (asset.needsFlushing) "Yes" else "No",
                asset.notes ?: "",
                if (asset.augmentedCare) "Yes" else "No",
                if (asset.lowUsageAsset) "Yes" else "No",
                asset.createdAt?.let { dateFormatter.format(it) } ?: "",
                asset.createdBy ?: "",
                asset.updatedAt?.let { dateFormatter.format(it) } ?: "",
                asset.updatedBy ?: ""
            ).map { "\"$it\"" }.joinToString(",")
        }
    }
}