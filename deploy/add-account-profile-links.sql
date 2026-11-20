-- One-time migration for the existing MySQL schema. Review and run separately from the app.
ALTER TABLE users
    ADD COLUMN athlete_profile_id BIGINT NULL,
    ADD COLUMN coach_profile_id BIGINT NULL,
    ADD CONSTRAINT uk_users_athlete_profile UNIQUE (athlete_profile_id),
    ADD CONSTRAINT uk_users_coach_profile UNIQUE (coach_profile_id),
    ADD CONSTRAINT fk_users_athlete_profile FOREIGN KEY (athlete_profile_id) REFERENCES athletes(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_users_coach_profile FOREIGN KEY (coach_profile_id) REFERENCES coaches(id) ON DELETE SET NULL;
