CREATE EXTENSION IF NOT EXISTS citext;

create table person (
  id serial primary key,
  nickname CITEXT COLLATE "en_US.utf8" not null UNIQUE,
  fullname text NOT NULL,
  email CITEXT COLLATE "en_US.utf8" NOT NULL UNIQUE,
  about text
);