
-- Create the drone table
CREATE TABLE drone (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(100) UNIQUE NOT NULL,
    model VARCHAR(50) NOT NULL,
    weight_limit DOUBLE NOT NULL,
    battery_capacity INT NOT NULL,
    state VARCHAR(50) NOT NULL
);

-- Create the medication table
CREATE TABLE medication (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    weight INT NOT NULL,
    code VARCHAR(100) UNIQUE NOT NULL,
    drone_id BIGINT,
    image_name VARCHAR(255),
    image_type VARCHAR(255),
    image_data BLOB,
    FOREIGN KEY (drone_id) REFERENCES drone(id)
);