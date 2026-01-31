-- Migration V3: Add genero and cuit columns to examples table

ALTER TABLE examples 
ADD COLUMN genero VARCHAR(1),
ADD COLUMN cuit VARCHAR(15);

-- Update existing records with default values
UPDATE examples SET genero = 'H', cuit = '20-' || dni || '-7' WHERE genero IS NULL;

-- Make columns NOT NULL after setting defaults
ALTER TABLE examples 
ALTER COLUMN genero SET NOT NULL,
ALTER COLUMN cuit SET NOT NULL;

-- Add check constraint for genero
ALTER TABLE examples 
ADD CONSTRAINT chk_genero CHECK (genero IN ('H', 'M'));

-- Create index on cuit
CREATE INDEX idx_examples_cuit ON examples(cuit);

-- Comments
COMMENT ON COLUMN examples.genero IS 'Género: H (Hombre) o M (Mujer)';
COMMENT ON COLUMN examples.cuit IS 'CUIT calculado basado en DNI y género';
