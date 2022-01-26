CREATE TABLE IF NOT EXISTS Test(
    id INTEGER NOT NULL  PRIMARY KEY AUTO_INCREMENT,
    name varchar(20),
    email varchar (50)
);
INSERT INTO Test values(1, 'John', 'johndoe@example.com');
