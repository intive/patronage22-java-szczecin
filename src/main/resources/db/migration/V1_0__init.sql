CREATE SCHEMA retro;

CREATE TABLE IF NOT EXISTS retro.user_data (
    uid VARCHAR(128) NOT NULL,
    name VARCHAR(64),
    PRIMARY KEY(uid)
);

CREATE TABLE IF NOT EXISTS retro.board (
    id SERIAL,
    name VARCHAR(32) NOT NULL,
    state VARCHAR(16) NOT NULL,
    creator_uid VARCHAR(128),
    PRIMARY KEY(id),
    FOREIGN KEY(creator_uid) REFERENCES retro.user_data(uid)
);