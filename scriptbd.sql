CREATE SCHEMA IF NOT EXISTS popsdb ;
USE popsdb;

-- Table team

CREATE TABLE IF NOT EXISTS team (
  id BIGINT NOT NULL AUTO_INCREMENT,
  fk_approver BIGINT NULL DEFAULT NULL,
  description VARCHAR(100) NULL DEFAULT NULL,
  name VARCHAR(60) NOT NULL,
  fk_project BIGINT NOT NULL,
  sprint_duration INT NOT NULL,
  status TINYINT(1) NOT NULL,
  PRIMARY KEY (id))
ENGINE = InnoDB;


-- Table allocation


CREATE TABLE IF NOT EXISTS allocation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  allocated_hours INT NOT NULL,
  fk_person BIGINT NOT NULL,
  position VARCHAR(255) NOT NULL,
  started_at DATE NOT NULL,
  fk_team BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_allocation_team
    FOREIGN KEY (fk_team) REFERENCES team (id)
) ENGINE = InnoDB;


-- Table allocation_history

CREATE TABLE IF NOT EXISTS allocation_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  allocated_hours INT NOT NULL,
  ended_at DATE NOT NULL,
  person_id BIGINT NOT NULL,
  position VARCHAR(255) NOT NULL,
  started_at DATE NOT NULL,
  fk_person BIGINT NOT NULL,
  fk_team BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_allocation_history_team
    FOREIGN KEY (fk_team)
    REFERENCES team (id)
) ENGINE = InnoDB;
