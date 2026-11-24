const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { spawnSync } = require('node:child_process');
const { test } = require('node:test');

const client = process.env.MYSQL_TEST_CLIENT;
if (!client) throw new Error('Set MYSQL_TEST_CLIENT to the MySQL 8 client executable.');

const args = [
  '--no-defaults', '--protocol=TCP', '--host=127.0.0.1', '--port=13317',
  '--user=root', '--skip-password', '--batch', '--raw', '--skip-column-names', '--default-character-set=utf8mb4',
];

function execute(sql) {
  const result = spawnSync(client, args, { input: sql, encoding: 'utf8' });
  if (result.error) throw result.error;
  return result;
}

function sql(query) {
  const result = execute(query);
  assert.equal(result.status, 0, result.stderr);
  return result.stdout.trim();
}

const check = readFileSync('deploy/check-account-profile-links.sql', 'utf8');
const ddl = readFileSync('deploy/add-account-profile-links.sql', 'utf8');
const fixture = readFileSync('deploy/exam-demo/01-schema.sql', 'utf8')
  .replaceAll('olympic_exam_demo', 'olympic_mysql_candidate');
const schema = 'USE olympic_mysql_candidate;\n';

function applyProfileLinks() {
  switch (sql(schema + check)) {
    case 'V1_ABSENT': sql(schema + ddl); break;
    case 'V1_COMPLETE': return 'no-op';
    default: throw new Error('Unexpected account profile link schema');
  }
  assert.equal(sql(schema + check), 'V1_COMPLETE');
  return 'applied';
}

test('isolated MySQL fixture, account profile migration, and storage constraints', () => {
  const [version, datadir] = sql('SELECT @@version, @@datadir;').split('\t');
  assert.match(version, /^8\.0\./);
  assert.match(datadir.replaceAll('\\', '/'), /\/target\/mysql-sandbox-13317\/?$/);
  assert.match(sql('SELECT @@sql_mode;'), /STRICT_TRANS_TABLES|STRICT_ALL_TABLES/);

  sql(fixture);
  assert.equal(applyProfileLinks(), 'no-op');
  sql(schema + `
    INSERT INTO athletes (name) VALUES ('Synthetic athlete');
    INSERT INTO users (username,password,role,athlete_profile_id)
      VALUES ('fixture-café','synthetic-hash','ATHLETE',LAST_INSERT_ID());
    INSERT INTO nutrition_entries (date,calories,athlete_ref) VALUES ('2020-01-01',0,1);
  `);
  assert.equal(sql(schema + "SELECT COUNT(*) FROM users WHERE username='FIXTURE-CAFE';"), '1');
  for (const query of [
    "INSERT INTO users (username,password,role) VALUES ('FIXTURE-CAFE','test','ATHLETE');",
    "INSERT INTO users (username,password,role) VALUES ('another','test','UNKNOWN');",
    "INSERT INTO athletes (name) VALUES (REPEAT('a',121));",
    "INSERT INTO nutrition_entries (date,athlete_ref) VALUES ('2020-01-01',99);",
    "INSERT INTO nutrition_entries (date,athlete_ref,notes) VALUES ('2020-01-01',1,REPEAT('a',301));",
  ]) {
    assert.notEqual(execute(schema + query).status, 0);
  }
  assert.equal(sql(schema + 'SELECT COUNT(*) FROM nutrition_entries WHERE recorded_at IS NOT NULL AND calories = 0;'), '1');

  sql(schema + `
    ALTER TABLE users
      DROP FOREIGN KEY fk_users_athlete_profile, DROP FOREIGN KEY fk_users_coach_profile,
      DROP INDEX uk_users_athlete_profile, DROP INDEX uk_users_coach_profile,
      DROP COLUMN athlete_profile_id, DROP COLUMN coach_profile_id;
  `);
  assert.equal(sql(schema + check), 'V1_ABSENT');
  assert.equal(applyProfileLinks(), 'applied');
  assert.equal(applyProfileLinks(), 'no-op');
  sql(schema + 'UPDATE users SET athlete_profile_id=1;');
  assert.notEqual(execute(schema + "INSERT INTO users (username,password,role,athlete_profile_id) VALUES ('duplicate-link','test','ATHLETE',1);").status, 0);
  sql(schema + 'DELETE FROM nutrition_entries WHERE athlete_ref=1; DELETE FROM athletes WHERE id=1;');
  assert.equal(sql(schema + 'SELECT COUNT(*) FROM users WHERE athlete_profile_id IS NULL;'), '1');
  sql(schema + 'ALTER TABLE users DROP FOREIGN KEY fk_users_athlete_profile;');
  assert.equal(sql(schema + check), 'V1_PARTIAL_OR_DRIFTED');
  assert.throws(applyProfileLinks, /Unexpected/);
});
