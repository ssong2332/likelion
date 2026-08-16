CREATE TABLE collaboration_styles (
    id BIGINT NOT NULL,
    tone VARCHAR(50) NOT NULL,
    directness VARCHAR(50) NOT NULL,
    detail_level VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_collaboration_styles_singleton CHECK (id = 1)
);
