-- Reladomo quotes "name" as a reserved word, so the column must be created quoted and lower case.
CREATE TABLE ITEM (
    id BIGINT NOT NULL PRIMARY KEY,
    "name" VARCHAR(256)
);
