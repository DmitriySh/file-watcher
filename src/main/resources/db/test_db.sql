-- psql -U postgres -a -f <file_path>
CREATE DATABASE "test_db";
\c "test_db";

CREATE TABLE "entry" (
  "id"            BIGSERIAL                   NOT NULL,
  "version"       BIGINT                      NOT NULL,
  "text"          TEXT                        NOT NULL,
  "creation_date" TIMESTAMP WITHOUT TIME ZONE NOT NULL,

  CONSTRAINT "entry_id_pk" PRIMARY KEY ("id")
);



