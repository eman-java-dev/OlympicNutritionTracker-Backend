-- Run once on the disposable exam instance at 127.0.0.1:3307.
CREATE DATABASE olympic_exam_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE olympic_exam_demo;

CREATE TABLE coaches (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    specialty VARCHAR(120),
    email VARCHAR(160),
    phone VARCHAR(40)
) ENGINE=InnoDB;

CREATE TABLE athletes (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    age INT,
    gender VARCHAR(10),
    height DOUBLE,
    weight DOUBLE,
    coach_id BIGINT,
    CONSTRAINT fk_exam_athlete_coach FOREIGN KEY (coach_id) REFERENCES coaches(id)
) ENGINE=InnoDB;

CREATE TABLE consultations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(300) NOT NULL,
    scheduled_at DATETIME(6) NOT NULL,
    athlete_ref BIGINT NOT NULL,
    coach_ref BIGINT NOT NULL,
    CONSTRAINT fk_exam_consultation_athlete FOREIGN KEY (athlete_ref) REFERENCES athletes(id),
    CONSTRAINT fk_exam_consultation_coach FOREIGN KEY (coach_ref) REFERENCES coaches(id)
) ENGINE=InnoDB;

CREATE TABLE nutrition_entries (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `date` DATE NOT NULL,
    calories INT,
    protein INT,
    carbs INT,
    fat INT,
    notes VARCHAR(300),
    athlete_ref BIGINT NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exam_nutrition_athlete FOREIGN KEY (athlete_ref) REFERENCES athletes(id)
) ENGINE=InnoDB;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'ATHLETE', 'COACH') NOT NULL,
    athlete_profile_id BIGINT NULL,
    coach_profile_id BIGINT NULL,
    CONSTRAINT uk_exam_username UNIQUE (username),
    CONSTRAINT uk_users_athlete_profile UNIQUE (athlete_profile_id),
    CONSTRAINT uk_users_coach_profile UNIQUE (coach_profile_id),
    CONSTRAINT fk_users_athlete_profile FOREIGN KEY (athlete_profile_id) REFERENCES athletes(id) ON DELETE SET NULL,
    CONSTRAINT fk_users_coach_profile FOREIGN KEY (coach_profile_id) REFERENCES coaches(id) ON DELETE SET NULL
) ENGINE=InnoDB;
