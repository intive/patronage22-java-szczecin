CREATE TABLE IF NOT EXISTS retro.board_card (
    id SERIAL,
    board_id INTEGER,
    text VARCHAR(128),
    "column" VARCHAR(16) NOT NULL,
    creator_uid VARCHAR(128),
    PRIMARY KEY(id),
    FOREIGN KEY(board_id) REFERENCES retro.board(id),
    FOREIGN KEY(creator_uid) REFERENCES retro.user_data(uid)
);

CREATE TABLE IF NOT EXISTS retro.board_card_action (
    id SERIAL,
    card_id INTEGER,
    text VARCHAR(128),
    PRIMARY KEY(id),
    FOREIGN KEY(card_id) REFERENCES retro.board_card(id)
);

CREATE TABLE IF NOT EXISTS retro.board_card_votes (
    card_id INTEGER,
    voter_uid VARCHAR(128),
    count INTEGER,
    PRIMARY KEY(card_id,voter_uid),
    FOREIGN KEY(card_id) REFERENCES retro.board_card(id),
    FOREIGN KEY(voter_uid) REFERENCES retro.user_data(uid)
);

CREATE TABLE IF NOT EXISTS retro.users_boards (
   board_id INTEGER,
   user_uid VARCHAR(128),
   FOREIGN KEY(board_id) REFERENCES retro.board(id),
   FOREIGN KEY(user_uid) REFERENCES retro.user_data(uid)
);