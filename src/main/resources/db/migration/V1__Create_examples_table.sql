-- Migration V1: Create examples table
-- Crear tabla examples con campos nombre, apellido, dni

CREATE TABLE IF NOT EXISTS examples (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Crear índice en DNI para búsquedas rápidas
CREATE INDEX idx_examples_dni ON examples(dni);

-- Comentarios
COMMENT ON TABLE examples IS 'Tabla de ejemplos con información básica';
COMMENT ON COLUMN examples.dni IS 'Documento Nacional de Identidad (único)';
