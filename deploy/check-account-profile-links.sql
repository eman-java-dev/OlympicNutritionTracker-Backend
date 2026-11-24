-- Inspect the selected schema with a read-only operator account.
-- BASE_SCHEMA_REVIEW means the expected fixture prerequisites were not found.
WITH base_tables AS (
    SELECT COUNT(*) AS compatible
    FROM information_schema.tables t
    JOIN information_schema.columns c
      ON c.table_schema = t.table_schema AND c.table_name = t.table_name AND c.column_name = 'id'
    WHERE t.table_schema = DATABASE() AND t.table_name IN ('users', 'athletes', 'coaches')
      AND t.engine = 'InnoDB' AND c.column_type = 'bigint' AND c.column_key = 'PRI'
), profile_columns AS (
    SELECT COUNT(*) AS found,
           COUNT(CASE WHEN column_type = 'bigint' AND is_nullable = 'YES'
                 AND column_default IS NULL AND extra = '' THEN 1 END) AS valid
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'users'
      AND column_name IN ('athlete_profile_id', 'coach_profile_id')
), profile_indexes AS (
    SELECT COUNT(*) AS found,
           COUNT(CASE WHEN column_name = IF(index_name = 'uk_users_athlete_profile',
                 'athlete_profile_id', 'coach_profile_id') AND non_unique = 0 AND seq_in_index = 1
                 AND sub_part IS NULL THEN 1 END) AS valid
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'users'
      AND index_name IN ('uk_users_athlete_profile', 'uk_users_coach_profile')
), profile_foreign_keys AS (
    SELECT COUNT(*) AS found,
           COUNT(CASE WHEN k.column_name = IF(k.constraint_name = 'fk_users_athlete_profile',
                    'athlete_profile_id', 'coach_profile_id')
                 AND k.referenced_table_schema = DATABASE()
                 AND k.referenced_table_name = IF(k.constraint_name = 'fk_users_athlete_profile', 'athletes', 'coaches')
                 AND k.referenced_column_name = 'id' AND k.ordinal_position = 1 AND r.delete_rule = 'SET NULL'
                 THEN 1 END) AS valid
    FROM information_schema.key_column_usage k
    JOIN information_schema.referential_constraints r
      ON r.constraint_schema = k.constraint_schema AND r.constraint_name = k.constraint_name
         AND r.table_name = k.table_name
    WHERE k.table_schema = DATABASE() AND k.table_name = 'users'
      AND k.constraint_name IN ('fk_users_athlete_profile', 'fk_users_coach_profile')
)
SELECT CASE
    WHEN b.compatible <> 3 THEN 'BASE_SCHEMA_REVIEW'
    WHEN c.found = 0 AND i.found = 0 AND f.found = 0 THEN 'V1_ABSENT'
    WHEN c.found = 2 AND c.valid = 2 AND i.found = 2 AND i.valid = 2 AND f.found = 2 AND f.valid = 2
      THEN 'V1_COMPLETE'
    ELSE 'V1_PARTIAL_OR_DRIFTED'
END AS account_profile_links
FROM base_tables b CROSS JOIN profile_columns c CROSS JOIN profile_indexes i CROSS JOIN profile_foreign_keys f;
