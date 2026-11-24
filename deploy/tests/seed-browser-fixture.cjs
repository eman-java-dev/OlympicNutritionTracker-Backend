const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { spawnSync } = require('node:child_process');

const { MYSQL_TEST_CLIENT: mysql, JAVA_TEST_EXE: java, BCRYPT_TEST_JAR: cryptoJar, BROWSER_TEST_PASSWORD: password } = process.env;
if (!mysql || !java || !cryptoJar || !password) throw new Error('Missing disposable browser fixture settings.');

function processOutput(command, args, input) {
  const result = spawnSync(command, args, { input, encoding: 'utf8' });
  if (result.error) throw result.error;
  if (result.status !== 0) throw new Error('Disposable fixture command failed.');
  return result.stdout.trim();
}
function query(sql) {
  return processOutput(mysql, [
    '--no-defaults', '--protocol=TCP', '--host=127.0.0.1', '--port=13317',
    '--user=root', '--skip-password', '--batch', '--raw', '--skip-column-names',
  ], sql);
}

const datadir = query('SELECT @@datadir;').replaceAll('\\', '/');
assert.match(datadir, /\/target\/mysql-sandbox-13317\/?$/);
const hash = processOutput(java, ['--class-path', cryptoJar, 'deploy/exam-demo/ExamPasswordHash.java'], password + '\n');
if (!/^\$2[aby]\$\d{2}\$[./a-zA-Z0-9]{53}$/.test(hash)) throw new Error('Invalid fixture hash.');
const name = 'olympic_browser_fixture';
const schema = readFileSync('deploy/exam-demo/01-schema.sql', 'utf8').replaceAll('olympic_exam_demo', name);
const sample = readFileSync('deploy/exam-demo/02-sample-data.sql.template', 'utf8')
  .replaceAll('olympic_exam_demo', name).replaceAll('__EXAM_DEMO_BCRYPT_HASH__', hash);
query(schema + '\n' + sample + "\nINSERT INTO users (username,password,role,athlete_profile_id) VALUES ('browser_other_athlete','" + hash + "','ATHLETE',2);");
console.log('Disposable browser fixture created.');
