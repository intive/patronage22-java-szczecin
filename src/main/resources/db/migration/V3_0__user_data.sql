ALTER TABLE retro.user_data RENAME COLUMN name TO email;
ALTER TABLE retro.user_data ADD COLUMN display_name VARCHAR(64);
