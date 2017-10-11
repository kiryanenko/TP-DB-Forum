CREATE EXTENSION IF NOT EXISTS citext;


CREATE TABLE person (
  id SERIAL PRIMARY KEY,
  nickname CITEXT COLLATE "en_US.utf8" NOT NULL UNIQUE,
  fullname TEXT NOT NULL,
  email CITEXT COLLATE "en_US.utf8" NOT NULL UNIQUE,
  about TEXT
);

CREATE INDEX idx_person_id ON person (id);
CREATE INDEX idx_person_nickname ON person (nickname);
CREATE INDEX idx_person_email ON person (email);


CREATE TABLE forum (
  id SERIAL PRIMARY KEY,
  posts INTEGER DEFAULT 0,
  slug TEXT NOT NULL UNIQUE,
  title TEXT NOT NULL,
  threads INTEGER DEFAULT 0,
  person_id bigint references person(id)
);

CREATE INDEX idx_forum_slug ON forum (slug);
