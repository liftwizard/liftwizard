drop table if exists PERSON;

create table PERSON
(
    id bigint not null,
    full_name varchar(255) not null,
    job_title varchar(255) not null,
    system_from timestamp not null,
    system_to timestamp not null
);

