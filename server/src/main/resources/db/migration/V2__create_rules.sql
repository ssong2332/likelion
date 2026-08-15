CREATE TABLE rules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_rules_name UNIQUE (name)
);
