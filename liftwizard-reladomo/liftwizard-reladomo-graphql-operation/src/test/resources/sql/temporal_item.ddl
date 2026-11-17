CREATE TABLE TEMPORAL_ITEM (
    id BIGINT NOT NULL,
    system_from TIMESTAMP NOT NULL,
    system_to TIMESTAMP NOT NULL,
    PRIMARY KEY (id, system_to)
);
