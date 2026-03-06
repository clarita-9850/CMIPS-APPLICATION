-- =====================================================
-- V2: Create job_dependency table
-- Purpose: Defines DAG relationships between jobs
-- =====================================================

CREATE TABLE job_dependency (
    id                  BIGSERIAL PRIMARY KEY,
    job_id              BIGINT NOT NULL,
    depends_on_job_id   BIGINT NOT NULL,
    dependency_type     VARCHAR(50) DEFAULT 'SUCCESS',
    is_active           BOOLEAN DEFAULT true,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(100),

    CONSTRAINT fk_job_dependency_job FOREIGN KEY (job_id)
        REFERENCES job_definition(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_dependency_depends_on FOREIGN KEY (depends_on_job_id)
        REFERENCES job_definition(id) ON DELETE CASCADE,
    CONSTRAINT uk_job_dependency UNIQUE (job_id, depends_on_job_id),
    CONSTRAINT chk_no_self_dependency CHECK (job_id != depends_on_job_id),
    CONSTRAINT chk_dependency_type CHECK (dependency_type IN ('SUCCESS', 'COMPLETION', 'FAILURE'))
);

-- Indexes for dependency traversal
CREATE INDEX idx_job_dependency_job_id ON job_dependency(job_id) WHERE is_active = true;
CREATE INDEX idx_job_dependency_depends_on ON job_dependency(depends_on_job_id) WHERE is_active = true;

-- Comments
COMMENT ON TABLE job_dependency IS 'Defines directed acyclic graph (DAG) dependencies between jobs';
COMMENT ON COLUMN job_dependency.job_id IS 'The job that has the dependency';
COMMENT ON COLUMN job_dependency.depends_on_job_id IS 'The job that must complete first';
COMMENT ON COLUMN job_dependency.dependency_type IS 'SUCCESS: parent must succeed, COMPLETION: parent must complete (any status), FAILURE: parent must fail';
