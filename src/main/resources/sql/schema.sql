CREATE TABLE IF NOT EXISTS Record (
    hash VARCHAR(255),
    name VARCHAR(255),
    l BIGINT,
    o INTEGER,
    PRIMARY KEY (hash)
);

CREATE TABLE IF NOT EXISTS Load (
    time TIMESTAMP,
    machine VARCHAR(255),
    cpu INTEGER,
    mem INTEGER,
    records BIGINT,
    PRIMARY KEY (time, machine)
    );

CREATE TABLE IF NOT EXISTS TASKRUN (
    hash VARCHAR(255),
    name VARCHAR(255),
    started TIMESTAMP,
    finished TIMESTAMP,
    records BIGINT,
    PRIMARY KEY (hash)
);

CREATE TABLE IF NOT EXISTS TASK (
  id INTEGER
  name VARCHAR(255),
  createdAt TIMESTAMP,
  modifiedAt TIMESTAMP,
  active BOOLEAN,
  PRIMARY KEY (id)
);