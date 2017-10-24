CREATE EXTENSION IF NOT EXISTS CITEXT WITH SCHEMA public;


CREATE TABLE person (
  id SERIAL PRIMARY KEY,
  nickname CITEXT NOT NULL UNIQUE,
  fullname TEXT NOT NULL,
  email CITEXT NOT NULL UNIQUE,
  about TEXT
);

CREATE UNIQUE INDEX idx_person_nickname ON person (LOWER(nickname));
CREATE UNIQUE INDEX idx_person_email ON person (LOWER(email));


CREATE TABLE forum (
  id SERIAL PRIMARY KEY,
  posts INTEGER DEFAULT 0 CHECK (posts >= 0),
  slug CITEXT NOT NULL UNIQUE,
  title TEXT NOT NULL,
  threads INTEGER DEFAULT 0,
  person_id INTEGER REFERENCES person(id) NOT NULL
);

CREATE INDEX idx_forum_slug ON forum (LOWER(slug));

CREATE FUNCTION inc_forum_threads() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE forum SET threads = threads + 1 WHERE id = NEW.forum_id;
    RETURN NULL;
  END
$$ LANGUAGE plpgsql;


CREATE TABLE thread (
  id SERIAL PRIMARY KEY,
  author_id INTEGER REFERENCES person(id) NOT NULL,
  forum_id INTEGER REFERENCES forum(id) NOT NULL,
  title TEXT NOT NULL,
  message TEXT NOT NULL,
  slug CITEXT NULL UNIQUE,
  votes INTEGER DEFAULT 0,
  created TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_thread_forum ON thread (forum_id, created);
CREATE INDEX idx_thread_forum_author ON thread (forum_id, author_id);
CREATE UNIQUE INDEX idx_thread_slug ON thread (LOWER(slug));

CREATE FUNCTION inc_thread_votes() RETURNS TRIGGER AS $$
BEGIN
  UPDATE thread SET votes = votes + NEW.voice WHERE id = NEW.thread_id;
  RETURN NULL;
END
$$ LANGUAGE plpgsql;

CREATE FUNCTION set_thread_votes() RETURNS TRIGGER AS $$
BEGIN
  UPDATE thread SET votes = votes - OLD.voice + NEW.voice WHERE id = NEW.thread_id;
  RETURN NULL;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER add_thread AFTER INSERT ON thread FOR EACH ROW EXECUTE PROCEDURE inc_forum_threads();


CREATE TABLE post (
  id SERIAL PRIMARY KEY,
  author_id INTEGER REFERENCES person(id) NOT NULL,
  thread_id INTEGER REFERENCES thread(id) NOT NULL,
  parent INTEGER REFERENCES post(id) NULL DEFAULT NULL,
  message TEXT NOT NULL DEFAULT now(),
  created TIMESTAMP NOT NULL DEFAULT now(),
  is_edited BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_post_thread_created_id ON post (thread_id, created, id);
CREATE INDEX idx_post_forum_author ON post (thread_id, author_id);


CREATE TABLE vote (
  person_id INTEGER REFERENCES person(id) NOT NULL,
  thread_id INTEGER REFERENCES thread(id) NOT NULL,
  voice SMALLINT NOT NULL CHECK (voice = 1 OR voice = -1)
);

CREATE UNIQUE INDEX idx_vote_person_thread ON vote (thread_id, person_id);

CREATE TRIGGER add_vote AFTER INSERT ON vote FOR EACH ROW EXECUTE PROCEDURE inc_thread_votes();
CREATE TRIGGER set_vote AFTER UPDATE ON vote FOR EACH ROW EXECUTE PROCEDURE set_thread_votes();
