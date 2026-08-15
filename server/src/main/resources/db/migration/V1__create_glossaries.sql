CREATE TABLE glossaries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    term VARCHAR(100) NOT NULL,
    rule_text VARCHAR(255) NOT NULL,
    note VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_glossaries_term UNIQUE (term)
);
