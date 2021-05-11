--- X|FIXME THa check foreign keys? and other constrains
DROP TABLE IF EXISTS monitored_endpoint_dbo;
DROP TABLE IF EXISTS monitoring_result_dbo;
DROP TABLE IF EXISTS user_dbo;

CREATE TABLE IF NOT EXISTS user_dbo (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    username VARCHAR(100) UNIQUE,
    email VARCHAR(100) UNIQUE,
    api_key VARCHAR(200) NOT NULL UNIQUE,
    version BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS monitored_endpoint_dbo (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    monitored_interval BIGINT NOT NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    next_check_at TIMESTAMP NOT NULL,
    FOREIGN KEY (owner_user_id)
        REFERENCES user_dbo(id)
        ON DELETE CASCADE,
    CONSTRAINT one_url_per_user UNIQUE KEY (owner_user_id, url),
    CONSTRAINT one_name_per_user UNIQUE KEY (owner_user_id, name)
);

CREATE TABLE IF NOT EXISTS monitoring_result_dbo (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    checked_at TIMESTAMP NOT NULL,
    status_code VARCHAR(60),
    content_type VARCHAR(60),
    payload MEDIUMTEXT,
    monitored_endpoint_id VARCHAR(36),
    url VARCHAR(4096),
    error TEXT
);
