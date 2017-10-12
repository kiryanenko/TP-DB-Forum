CREATE EXTENSION IF NOT EXISTS citext;


CREATE TABLE person (
  id SERIAL PRIMARY KEY,
  nickname CITEXT COLLATE "en_US.utf8" NOT NULL UNIQUE,
  fullname TEXT NOT NULL,
  email CITEXT COLLATE "en_US.utf8" NOT NULL UNIQUE,
  about TEXT
);

CREATE INDEX idx_person_nickname ON person (nickname);
CREATE INDEX idx_person_email ON person (email);


CREATE TABLE forum (
  id SERIAL PRIMARY KEY,
  posts INTEGER DEFAULT 0,
  slug TEXT NOT NULL UNIQUE,
  title TEXT NOT NULL,
  threads INTEGER DEFAULT 0,
  person_id INTEGER REFERENCES person(id) NOT NULL
);

CREATE INDEX idx_forum_slug ON forum (slug);


CREATE TABLE thread (
  id SERIAL PRIMARY KEY,
  author_id INTEGER REFERENCES person(id) NOT NULL,
  forum_id INTEGER REFERENCES forum(id) NOT NULL,
  title TEXT NOT NULL,
  message TEXT NOT NULL,
  slug TEXT NULL UNIQUE,
  votes INTEGER DEFAULT 0,
  created TIMESTAMP NOT NULL
);

CREATE INDEX idx_thread_forum ON thread (forum_id, created);
CREATE INDEX idx_thread_slug ON thread (slug);
