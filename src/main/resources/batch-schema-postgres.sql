-- Spring Batch metadata tables within batch_meta schema
DROP TABLE IF EXISTS batch_meta.batch_step_execution_context;
DROP TABLE IF EXISTS batch_meta.batch_job_execution_context;
DROP TABLE IF EXISTS batch_meta.batch_step_execution;
DROP TABLE IF EXISTS batch_meta.batch_job_execution_params;
DROP TABLE IF EXISTS batch_meta.batch_job_execution;
DROP TABLE IF EXISTS batch_meta.batch_job_instance;

DROP SEQUENCE IF EXISTS batch_meta.batch_step_execution_seq;
DROP SEQUENCE IF EXISTS batch_meta.batch_job_execution_seq;
DROP SEQUENCE IF EXISTS batch_meta.batch_job_seq;

CREATE TABLE batch_meta.batch_job_instance
(
    job_instance_id BIGINT       NOT NULL PRIMARY KEY,
    version         BIGINT,
    job_name        VARCHAR(100) NOT NULL,
    job_key         VARCHAR(32)  NOT NULL,
    CONSTRAINT batch_job_instance_job_name_job_key_uq UNIQUE (job_name, job_key)
);

CREATE TABLE batch_meta.batch_job_execution
(
    job_execution_id           BIGINT    NOT NULL PRIMARY KEY,
    version                    BIGINT,
    job_instance_id            BIGINT    NOT NULL,
    create_time                TIMESTAMP NOT NULL,
    start_time                 TIMESTAMP,
    end_time                   TIMESTAMP,
    status                     VARCHAR(10),
    exit_code                  VARCHAR(2500),
    exit_message               VARCHAR(2500),
    last_updated               TIMESTAMP,
    job_configuration_location VARCHAR(2500),
    CONSTRAINT batch_job_execution_instance_fk FOREIGN KEY (job_instance_id)
        REFERENCES batch_meta.batch_job_instance (job_instance_id)
);

CREATE TABLE batch_meta.batch_job_execution_params
(
    job_execution_id BIGINT       NOT NULL,
    parameter_name   VARCHAR(100) NOT NULL,
    parameter_type   VARCHAR(100) NOT NULL,
    parameter_value  VARCHAR(2500),
    identifying      CHAR(1)      NOT NULL,
    CONSTRAINT batch_job_execution_params_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch_meta.batch_job_execution (job_execution_id)
);

CREATE TABLE batch_meta.batch_step_execution
(
    step_execution_id  BIGINT       NOT NULL PRIMARY KEY,
    version            BIGINT       NOT NULL,
    step_name          VARCHAR(100) NOT NULL,
    job_execution_id   BIGINT       NOT NULL,
    start_time         TIMESTAMP,
    end_time           TIMESTAMP,
    status             VARCHAR(10),
    commit_count       BIGINT,
    read_count         BIGINT,
    filter_count       BIGINT,
    write_count        BIGINT,
    read_skip_count    BIGINT,
    write_skip_count   BIGINT,
    process_skip_count BIGINT,
    rollback_count     BIGINT,
    exit_code          VARCHAR(2500),
    exit_message       VARCHAR(2500),
    last_updated       TIMESTAMP,
    create_time        TIMESTAMP    NOT NULL,
    CONSTRAINT batch_step_execution_job_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch_meta.batch_job_execution (job_execution_id)
);

CREATE TABLE batch_meta.batch_step_execution_context
(
    step_execution_id  BIGINT        NOT NULL PRIMARY KEY,
    short_context      VARCHAR(2500) NOT NULL,
    serialized_context TEXT,
    CONSTRAINT batch_step_execution_context_fk FOREIGN KEY (step_execution_id)
        REFERENCES batch_meta.batch_step_execution (step_execution_id)
);

CREATE TABLE batch_meta.batch_job_execution_context
(
    job_execution_id   BIGINT        NOT NULL PRIMARY KEY,
    short_context      VARCHAR(2500) NOT NULL,
    serialized_context TEXT,
    CONSTRAINT batch_job_execution_context_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch_meta.batch_job_execution (job_execution_id)
);

CREATE SEQUENCE batch_meta.batch_step_execution_seq START WITH 1 MINVALUE 1;
CREATE SEQUENCE batch_meta.batch_job_execution_seq START WITH 1 MINVALUE 1;
CREATE SEQUENCE batch_meta.batch_job_seq START WITH 1 MINVALUE 1;

SELECT setval('batch_meta.batch_step_execution_seq', 1, false);
SELECT setval('batch_meta.batch_job_execution_seq', 1, false);
SELECT setval('batch_meta.batch_job_seq', 1, false);
