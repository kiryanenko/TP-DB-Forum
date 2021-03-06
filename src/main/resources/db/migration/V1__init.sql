CREATE TABLE person (
  id SERIAL PRIMARY KEY,
  nickname TEXT COLLATE "ucs_basic" NOT NULL UNIQUE,
  fullname TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  about TEXT
);

CREATE UNIQUE INDEX idx_person_nickname ON person (LOWER(nickname));
CREATE UNIQUE INDEX idx_person_email ON person (LOWER(email));


CREATE TABLE forum (
  id SERIAL PRIMARY KEY,
  posts INTEGER DEFAULT 0 CHECK (posts >= 0),
  slug TEXT NOT NULL UNIQUE,
  title TEXT NOT NULL,
  threads INTEGER DEFAULT 0,
  person_nickname TEXT REFERENCES person(nickname) NOT NULL,
  person_id INTEGER REFERENCES person(id) NOT NULL
);

CREATE UNIQUE INDEX idx_forum_slug ON forum (LOWER(slug));

CREATE FUNCTION inc_forum_threads() RETURNS TRIGGER AS $$
  BEGIN
    UPDATE forum SET threads = threads + 1 WHERE id = NEW.forum_id;
    RETURN NULL;
  END
$$ LANGUAGE plpgsql;


CREATE TABLE IF NOT EXISTS forum_person (
  person_id INTEGER REFERENCES person(id) NOT NULL,
  forum_id INTEGER REFERENCES forum(id) NOT NULL,
  UNIQUE (person_id, forum_id)
);

CREATE INDEX idx_forum_person ON forum_person (forum_id, person_id);


CREATE TABLE thread (
  id SERIAL PRIMARY KEY,
  author TEXT REFERENCES person(nickname) NOT NULL,
  author_id INTEGER REFERENCES person(id) NOT NULL,
  forum TEXT REFERENCES forum(slug) NOT NULL,
  forum_id INTEGER REFERENCES forum(id) NOT NULL,
  title TEXT NOT NULL,
  message TEXT NOT NULL,
  slug TEXT NULL UNIQUE,
  votes INTEGER DEFAULT 0,
  created TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_thread_forum ON thread (forum_id, created);
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


CREATE SEQUENCE posts_id_seq;

CREATE TABLE post (
  id SERIAL PRIMARY KEY,
  author TEXT REFERENCES person(nickname) NOT NULL,
  author_id INTEGER REFERENCES person(id) NOT NULL,
  thread_id INTEGER REFERENCES thread(id) NOT NULL,
  forum_id INTEGER REFERENCES forum(id) NOT NULL,
  forum TEXT REFERENCES forum(slug) NOT NULL,
  parent INTEGER REFERENCES post(id) NULL DEFAULT NULL,
  root INTEGER REFERENCES post(id) NULL DEFAULT NULL,
  message TEXT NOT NULL DEFAULT now(),
  created TIMESTAMP NOT NULL DEFAULT now(),
  is_edited BOOLEAN NOT NULL DEFAULT FALSE,
  path INTEGER[] NOT NULL
);

CREATE INDEX idx_post_roots ON post (thread_id, path, id) WHERE parent IS NULL;
CREATE INDEX idx_post_roots_desc ON post (thread_id, path, id DESC) WHERE parent IS NULL;
CREATE INDEX idx_post_thread_path ON post (thread_id, path, id);
CREATE INDEX idx_post_thread ON post (thread_id, id);
CREATE INDEX idx_post_root ON post (root, path);
CREATE INDEX idx_post_root_desc ON post (root, path DESC);
CREATE INDEX idx_post_id ON post (id, path);


CREATE TABLE vote (
  id SERIAL PRIMARY KEY,
  person_id INTEGER REFERENCES person(id) NOT NULL,
  thread_id INTEGER REFERENCES thread(id) NOT NULL,
  voice SMALLINT NOT NULL CHECK (voice = 1 OR voice = -1)
);

CREATE INDEX idx_vote_person_thread ON vote (thread_id, person_id, id, voice);

CREATE TRIGGER add_vote AFTER INSERT ON vote FOR EACH ROW EXECUTE PROCEDURE inc_thread_votes();
CREATE TRIGGER set_vote AFTER UPDATE ON vote FOR EACH ROW EXECUTE PROCEDURE set_thread_votes();
