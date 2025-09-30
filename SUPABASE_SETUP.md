# Supabase Setup Instructions

## 🎯 Your Project Configuration
- **Supabase URL**: https://ydudfioxgxyglzuckcnp.supabase.co
- **Anon Key**: Already configured in the app
- **Project**: Ready for asset management

## 📋 Setup Steps

### 1. Create Database Schema
1. Go to your Supabase dashboard: https://ydudfioxgxyglzuckcnp.supabase.co
2. Navigate to **SQL Editor**
3. Copy and paste the entire content from `supabase_schema.sql`
4. Click **Run** to create the database structure

### 2. Verify Database Setup
After running the SQL, you should see:
- `assets` table with all 25 columns
- Proper indexes for performance
- RLS policies enabled
- Sample data inserted

### 3. Test the Connection
Build and run your Android app:
```bash
./gradlew build
```

The app will now:
- Store assets locally in Room database
- Sync with your Supabase database
- Handle offline scenarios gracefully

## 🔄 Data Import Process

### Import Your Excel Data

1. **Prepare Your Excel File**:
   - Ensure it has the 25 columns as defined
   - Save as CSV if needed for easier processing

2. **Use the Import Helper**:
   ```kotlin
   // Example usage in your app
   val csvRows = readCsvFile(file)
   val assets = csvRows.mapNotNull { ExcelImportHelper.parseExcelRow(it) }
   
   // Import to local database
   assets.forEach { asset ->
       viewModel.insert(asset.toAsset())
   }
   
   // Sync to Supabase
   syncManager.syncAllAssets()
   ```

3. **Bulk Import via Supabase Dashboard**:
   - Convert Excel to CSV
   - Use Supabase Table Editor > Import data
   - Map columns to match the schema

## 🛠️ Database Schema Overview

### Main Asset Fields
- `id` - Primary key (auto-generated)
- `asset_barcode` - Unique barcode identifier
- `primary_identifier` - Main asset identifier
- `asset_type` - Category/type of asset
- `status` - Current status (active, maintenance, etc.)

### Location Fields
- `wing`, `wing_short` - Building wing information
- `room`, `room_number`, `room_name` - Room details
- `floor`, `floor_words` - Floor information

### Filter Management
- `filter_needed` - Boolean: requires filter
- `filter_expiry_date` - When filter expires
- `filter_type` - Type of filter used

### Maintenance & Care
- `needs_flushing` - Boolean: requires flushing
- `augmented_care` - Boolean: needs special care
- `low_usage_asset` - Boolean: rarely used

### Audit Trail
- `created_at`, `created_by` - Creation tracking
- `updated_at`, `updated_by` - Modification tracking
- `sync_status` - Synchronization status

## 🔒 Security Configuration

The database uses Row Level Security (RLS):
- **Read**: Public access (adjust as needed)
- **Write**: Authenticated users only
- **Update/Delete**: Authenticated users only

### To Modify Security:
```sql
-- Example: Restrict to specific users
CREATE POLICY "Restrict to admin users" ON assets 
FOR ALL USING (auth.jwt() ->> 'role' = 'admin');
```

## 📊 Sync Status Tracking

Assets have sync status tracking:
- `PENDING` - Needs to be synced to Supabase
- `SYNCED` - Successfully synchronized
- `ERROR` - Sync failed, manual intervention needed

## 🚀 Next Steps

1. **Run the schema setup** in your Supabase dashboard
2. **Build and test** the Android app
3. **Import your Excel data** using the helper utilities
4. **Test synchronization** between local and remote
5. **Configure authentication** if needed for multi-user access

## 📱 App Features Now Available

- ✅ **Local storage** with Room database
- ✅ **Cloud sync** with Supabase
- ✅ **Offline support** with sync when online
- ✅ **Excel import/export** compatibility
- ✅ **Real-time updates** (when implemented)
- ✅ **Audit trail** for all changes
- ✅ **Filter management** for maintenance schedules
- ✅ **Location tracking** with detailed room information

Your asset management system is now enterprise-ready with full cloud synchronization!