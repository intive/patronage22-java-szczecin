CREATE TABLE IF NOT EXISTS  Board_Card (
    id SERIAL,
    board_id INTEGER,
    text VARCHAR(128),
    column VARCHAR(16) NOT NULL,
    creator_uid VARCHAR(128),
    PRIMARY KEY(id),
    FOREIGN KEY(board_id) REFERENCES Board(id),
    FOREIGN KEY(creator_uid) REFERENCES User(uid)
);

CREATE TABLE IF NOT EXISTS  Board_Card_Action (
    id SERIAL,
    card_id INTEGER,
    text VARCHAR(128),
    PRIMARY KEY(id),
    FOREIGN KEY(card_id) REFERENCES Board_Card(id)
);

CREATE TABLE IF NOT EXISTS  Board_Card_Votes (
    card_id INTEGER,
    voter_uid VARCHAR(128),
    count INTEGER(16),
    PRIMARY KEY(card_id,voter_uid),
    FOREIGN KEY(card_id) REFERENCES Board_Card(id),
    FOREIGN KEY(voter_uid) REFERENCES User(uid)
);