CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE person (
  id SERIAL PRIMARY KEY,
  nickname CITEXT COLLATE "en_US.utf8" NOT NULL UNIQUE,
  fullname text NOT NULL,
  email CITEXT COLLATE "en_US.utf8" NOT NULL UNIQUE,
  about text
);