-- Migration V2: Insert initial example data
-- Insertar datos iniciales de ejemplo

INSERT INTO examples (nombre, apellido, dni, created_at, updated_at) 
VALUES 
    ('Juan', 'Pérez', '12345678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('María', 'García', '87654321', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Carlos', 'López', '11223344', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
