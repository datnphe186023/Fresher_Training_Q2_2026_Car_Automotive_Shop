-- V4: Create Inventory tables - Product, Supplier, StockMovement enhancements

-- Create suppliers table
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255),
    payment_terms VARCHAR(100),
    PRIMARY KEY (id),
    INDEX idx_supplier_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Update products table if not exists
CREATE TABLE IF NOT EXISTS products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(100) NOT NULL,
    sku VARCHAR(50) NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(10,2) NOT NULL,
    supplier_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    INDEX idx_product_sku (sku),
    INDEX idx_product_supplier_id (supplier_id),
    INDEX idx_product_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Update stock_movements table with new fields
ALTER TABLE stock_movements ADD COLUMN reason VARCHAR(30) DEFAULT 'OTHER' AFTER type;
ALTER TABLE stock_movements ADD COLUMN created_by VARCHAR(100) AFTER notes;
ALTER TABLE stock_movements ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE stock_movements ADD INDEX idx_sm_type (type);

-- Create stock alerts table
CREATE TABLE stock_alerts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    alert_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    resolved_by VARCHAR(100),
    notes LONGTEXT,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_alert_product_id (product_id),
    INDEX idx_alert_status (status),
    INDEX idx_alert_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create purchase_orders table
CREATE TABLE purchase_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_delivery_date DATETIME,
    actual_delivery_date DATETIME,
    total_amount DECIMAL(12,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes LONGTEXT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE RESTRICT,
    INDEX idx_po_supplier_id (supplier_id),
    INDEX idx_po_status (status),
    INDEX idx_po_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create purchase_order_items table
CREATE TABLE purchase_order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(12,2),
    received_quantity INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_poi_purchase_order_id (purchase_order_id),
    INDEX idx_poi_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index on stock_movements for product and date range queries
CREATE INDEX idx_sm_product_date ON stock_movements(product_id, movement_date);
