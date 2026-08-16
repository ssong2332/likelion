CREATE TABLE message_histories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    original_text TEXT NOT NULL,
    result_text TEXT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);
