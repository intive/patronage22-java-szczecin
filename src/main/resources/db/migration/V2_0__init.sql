CREATE TABLE IF NOT EXISTS  User (
    uid VARCHAR(128) NOT NULL,
    name VARCHAR(64),
    PRIMARY KEY(uid)
);

CREATE TABLE IF NOT EXISTS  Board (
    id SERIAL,
    name VARCHAR(32) NOT NULL,
    state VARCHAR(16) NOT NULL,
    creator_uid VARCHAR(128),
    PRIMARY KEY(id),
    FOREIGN KEY(creator_uid) REFERENCES User(uid)
);