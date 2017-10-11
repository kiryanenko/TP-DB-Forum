CREATE EXTENSION IF NOT EXISTS citext;


CREATE TABLE person (
  id SERIAL PRIMARY KEY,
  nickname CITEXT COLLATE "en_US.utf8" NOT NULL UNIQUE,
  fullname TEXT NOT NULL,
  email CITEXT COLLATE "en_US.utf8" NOT NULL UNIQUE,
  about TEXT
);


CREATE TABLE forum (
  id SERIAL PRIMARY KEY,
  posts INTEGER DEFAULT 0,
  slug TEXT NOT NULL UNIQUE,
  title TEXT NOT NULL,
  threads INTEGER DEFAULT 0,
  person_id bigint references person(id)
);
