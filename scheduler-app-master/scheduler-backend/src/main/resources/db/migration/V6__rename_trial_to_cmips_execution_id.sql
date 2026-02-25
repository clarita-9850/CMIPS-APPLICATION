-- Rename trial_execution_id to cmips_execution_id (CMIPS backend replaces Trial app)
ALTER TABLE execution_mapping RENAME COLUMN trial_execution_id TO cmips_execution_id;

DROP INDEX IF EXISTS idx_execution_trial_id;
CREATE INDEX idx_execution_cmips_id ON execution_mapping(cmips_execution_id) WHERE cmips_execution_id IS NOT NULL;

COMMENT ON COLUMN execution_mapping.cmips_execution_id IS 'Spring Batch execution ID from CMIPS backend';
