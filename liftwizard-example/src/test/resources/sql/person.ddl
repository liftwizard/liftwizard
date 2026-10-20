CREATE TABLE PERSON (
    id BIGINT NOT NULL,
    full_name VARCHAR(256) NOT NULL,
    job_title VARCHAR(256),
    system_from TIMESTAMP NOT NULL,
    system_to TIMESTAMP NOT NULL,
    PRIMARY KEY (id, system_to)
);

CREATE TABLE OBJECT_SEQUENCE (
    sequence_name VARCHAR(256) NOT NULL PRIMARY KEY,
    next_id BIGINT NOT NULL
);
