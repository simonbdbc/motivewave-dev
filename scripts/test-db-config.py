#!/usr/bin/env python3
"""Compile and exercise the actual DB boundaries with synthetic configuration.

This test makes no database connection; it tests missing-config refusals and
external file loading. Full Hikari/database/Gradle validation remains separate.
"""
from pathlib import Path
import os
import re
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]
LOADERS = [
 ('MotiveWave_Studies/src/sbdev/joy_ai_project/strategy/config/DbConfig.java', 'mw.strategy.dbConfig', 'MW_STRATEGY_DB_CONFIG'),
 ('MotiveWave_Studies/src/sbdev/joy_ai_project/pattern_strategy_algo/config/DbConfig.java', 'mw.pattern.dbConfig', 'MW_PATTERN_DB_CONFIG'),
 ('MotiveWave_Studies/PatternBacktest/src/main/java/backtest/config/DbConfig.java', 'backtest.dbConfig', 'BACKTEST_DB_CONFIG'),
 ('MotiveWave_Studies/PatternBacktestStrategy/src/main/java/backtest/config/DbConfig.java', 'matcher.dbConfig', 'MATCHER_DB_CONFIG'),
]
POOLS = [
 'MonitorAlgo/backend/src/main/java/com/monitoralgo/core/database/DatabaseManager.java',
 'MonitorAlgo/backend/src/main/java/com/monitoralgo/stats/database/DatabaseStatsManager.java',
 'MotiveWave_Studies/src/sbdev/joy_ai_project/strategy/database/DatabaseAlgoManager.java',
 'MotiveWave_Studies/src/sbdev/joy_ai_project/pattern_strategy_algo/database/DatabaseAlgoManager.java',
]
HARNESS = '''
public class ConfigProbe {
 public static void main(String[] args) throws Exception {
  Object cfg = Class.forName(args[0]).getMethod("getInstance").invoke(null);
  var get = cfg.getClass().getMethod("get", String.class);
  String[][] expected = {{"db.url", "jdbc:postgresql://127.0.0.1:1/demo"},
    {"db.user", "synthetic"}, {"db.password", "TEST_ONLY_FAKE_PASSWORD"},
    {"db.pool.max", "20"}, {"db.pool.min", "2"},
    {"db.pool.idleTimeout", "60000"}, {"db.pool.connectionTimeout", "30000"}};
  for (String[] entry : expected) {
   if (!entry[1].equals(get.invoke(cfg, entry[0]))) throw new AssertionError(entry[0]);
  }
  System.out.println("external config and pool defaults passed");
 }
}
'''
with tempfile.TemporaryDirectory(prefix='mw-db-config-') as tmp:
    tmp = Path(tmp)
    env = {k: v for k, v in os.environ.items() if k not in [e for _, _, e in LOADERS] and not k.endswith('DATASOURCE_PASSWORD') and not k.endswith('DATASOURCE_URL') and not k.endswith('DATASOURCE_USERNAME') and k not in ('JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', '_JAVA_OPTIONS')}
    fixture = tmp / 'private.properties'
    fixture.write_text('db.url=jdbc:postgresql://127.0.0.1:1/demo\ndb.user=synthetic\ndb.password=TEST_ONLY_FAKE_PASSWORD\n')
    os.chmod(fixture, 0o600)
    bad = tmp / 'empty-password.properties'
    bad.write_text('db.url=jdbc:postgresql://127.0.0.1:1/demo\ndb.user=synthetic\ndb.password=\n')
    harness = tmp / 'ConfigProbe.java'
    harness.write_text(HARNESS)
    for index, (path, prop, var) in enumerate(LOADERS):
        src = ROOT / path
        package = re.search(r'^package (.*);', src.read_text()).group(1)
        target = tmp / str(index)
        target.mkdir()
        subprocess.run(['javac', '-d', str(target), str(src), str(harness)], env=env, check=True, capture_output=True)
        # A packaged decoy must never satisfy the external config requirement.
        decoy = target / Path(package.replace('.', '/')) / 'db.properties'
        decoy.write_text(fixture.read_text())
        base = ['java', '-cp', str(target), 'ConfigProbe', package + '.DbConfig']
        for mode in ('missing', 'empty', 'missing-file', 'empty-password'):
            cmd = base[:]
            if mode != 'missing':
                value = '' if mode == 'empty' else str(tmp / 'not-present') if mode == 'missing-file' else str(bad)
                cmd.insert(1, f'-D{prop}={value}')
            result = subprocess.run(cmd, env=env, capture_output=True, text=True)
            assert result.returncode != 0 and ('Configuration DB manquante' in result.stderr or 'configuration DB externe' in result.stderr or 'Configurer ' in result.stderr), mode
        result = subprocess.run(base, env={**env, var: str(fixture)}, capture_output=True, text=True)
        assert result.returncode == 0, result.stderr
        cmd = base[:]
        cmd.insert(1, f'-D{prop}={fixture}')
        result = subprocess.run(cmd, env={**env, var: str(tmp / 'not-present')}, capture_output=True, text=True)
        assert result.returncode == 0, result.stderr
        print(f'loader {index + 1}: compiled, external property/env, defaults and four refusals passed')
    jars = [str(x) for x in (ROOT / 'MotiveWave_Studies/lib').glob('*.jar')]
    for index, path in enumerate(POOLS):
        target = tmp / ('pool' + str(index))
        target.mkdir()
        src = ROOT / path
        package = re.search(r'^package (.*);', src.read_text()).group(1)
        klass = package + '.' + src.stem
        probe = tmp / 'PoolProbe.java'
        probe.write_text('public class PoolProbe { public static void main(String[] args) throws Exception { Class.forName(args[0]); } }')
        subprocess.run(['javac', '-cp', os.pathsep.join(jars), '-d', str(target), str(src), str(probe)], env=env, capture_output=True, check=True)
        result = subprocess.run(['java', '-cp', os.pathsep.join([str(target), *jars]), 'PoolProbe', klass], env=env, capture_output=True, text=True)
        assert result.returncode != 0 and 'Configuration DB manquante' in result.stderr, result.stderr
        print(f'pool {index + 1}: compiled, missing configuration refused before connection')
    print('No live PostgreSQL, broker, replay or MotiveWave validation performed')
