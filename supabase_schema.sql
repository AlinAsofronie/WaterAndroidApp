-- Supabase SQL Schema for Asset Management
-- This creates the complete database structure based on your Excel export

-- Enable UUID extension for primary keys
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the main assets table
CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    
    -- Basic Asset Information
    asset_barcode VARCHAR(100) UNIQUE,
    primary_identifier VARCHAR(255) NOT NULL,
    secondary_identifier VARCHAR(255),
    asset_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) DEFAULT 'active',
    
    -- Location Information
    wing VARCHAR(100),
    wing_short VARCHAR(20),
    room VARCHAR(100),
    floor VARCHAR(50),
    floor_words VARCHAR(100),
    room_number VARCHAR(50),
    room_name VARCHAR(255),
    
    -- Filter Information
    filter_needed BOOLEAN DEFAULT false,
    filters_on BOOLEAN DEFAULT false,
    filter_installed_on DATE,
    filter_expiry_date DATE,
    filter_type VARCHAR(100),
    
    -- Maintenance Information
    needs_flushing BOOLEAN DEFAULT false,
    notes TEXT,
    
    -- Care and Usage Flags
    augmented_care BOOLEAN DEFAULT false,
    low_usage_asset BOOLEAN DEFAULT false,
    
    -- Legacy fields for compatibility with existing app
    name VARCHAR(255) NOT NULL DEFAULT '',
    description TEXT DEFAULT '',
    category VARCHAR(100) DEFAULT '',
    serial_number VARCHAR(100),
    purchase_date DATE,
    purchase_price DECIMAL(10,2) DEFAULT 0.00,
    current_value DECIMAL(10,2) DEFAULT 0.00,
    location VARCHAR(255) DEFAULT '',
    
    -- Sync and Tracking
    sync_status VARCHAR(20) DEFAULT 'synced',
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_by VARCHAR(255)
);

-- Create indexes for better performance
CREATE INDEX idx_assets_barcode ON assets(asset_barcode);
CREATE INDEX idx_assets_primary_identifier ON assets(primary_identifier);
CREATE INDEX idx_assets_asset_type ON assets(asset_type);
CREATE INDEX idx_assets_status ON assets(status);
CREATE INDEX idx_assets_wing ON assets(wing);
CREATE INDEX idx_assets_room ON assets(room);
CREATE INDEX idx_assets_filter_expiry ON assets(filter_expiry_date);
CREATE INDEX idx_assets_created_at ON assets(created_at);
CREATE INDEX idx_assets_sync_status ON assets(sync_status);

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = TIMEZONE('utc'::text, NOW());
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_assets_updated_at 
    BEFORE UPDATE ON assets 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Create enum types for better data integrity
CREATE TYPE asset_status_enum AS ENUM (
    'active',
    'maintenance', 
    'retired',
    'disposed',
    'in_use',
    'available',
    'out_of_service'
);

CREATE TYPE sync_status_enum AS ENUM (
    'pending',
    'synced',
    'error',
    'conflict'
);

-- Add constraints
ALTER TABLE assets ADD CONSTRAINT chk_status CHECK (status IN ('active', 'maintenance', 'retired', 'disposed', 'in_use', 'available', 'out_of_service'));
ALTER TABLE assets ADD CONSTRAINT chk_sync_status CHECK (sync_status IN ('pending', 'synced', 'error', 'conflict'));

-- Enable Row Level Security (RLS)
ALTER TABLE assets ENABLE ROW LEVEL SECURITY;

-- Create policies for RLS (adjust based on your authentication needs)
CREATE POLICY "Enable read access for all users" ON assets FOR SELECT USING (true);
CREATE POLICY "Enable insert for authenticated users only" ON assets FOR INSERT WITH CHECK (auth.role() = 'authenticated');
CREATE POLICY "Enable update for authenticated users only" ON assets FOR UPDATE USING (auth.role() = 'authenticated');
CREATE POLICY "Enable delete for authenticated users only" ON assets FOR DELETE USING (auth.role() = 'authenticated');

-- Create a view for simplified asset listing
CREATE VIEW asset_summary AS
SELECT 
    id,
    asset_barcode,
    primary_identifier,
    asset_type,
    status,
    wing,
    room,
    room_name,
    filter_needed,
    filter_expiry_date,
    needs_flushing,
    augmented_care,
    low_usage_asset,
    created_at,
    updated_at
FROM assets
ORDER BY primary_identifier;

-- Insert sample data based on typical asset management needs
INSERT INTO assets (
    asset_barcode, primary_identifier, secondary_identifier, asset_type, status,
    wing, wing_short, room, floor, room_name, filter_needed, name, category
) VALUES 
('BC001', 'LAPTOP-001', 'DEV-LAP-01', 'Laptop', 'active', 'North Wing', 'NW', 'NW-101', '1', 'Conference Room A', false, 'Dell Laptop', 'IT Equipment'),
('BC002', 'CHAIR-001', 'OFF-CHR-01', 'Office Chair', 'active', 'South Wing', 'SW', 'SW-201', '2', 'Office 201', false, 'Ergonomic Chair', 'Furniture'),
('BC003', 'FILTER-001', 'FLT-001', 'Water Filter', 'active', 'East Wing', 'EW', 'EW-301', '3', 'Kitchen', true, 'Water Filtration System', 'Equipment');