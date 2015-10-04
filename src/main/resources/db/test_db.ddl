-- psql -U postgres -a -f <ddl_path>
DROP DATABASE IF EXISTS  "test_db";

CREATE DATABASE "test_db";
\c "test_db";

CREATE TABLE "entry" (
  "id"            BIGSERIAL                   NOT NULL,
  "version"       BIGINT                      NOT NULL,
  "content"       TEXT                        NOT NULL,
  "creation_date" TIMESTAMP WITHOUT TIME ZONE NOT NULL,

  CONSTRAINT "entry_id_pk" PRIMARY KEY ("id")
);



