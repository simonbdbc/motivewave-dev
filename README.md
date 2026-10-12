# MotiveWave Dev

Collection historique d'études et stratégies Java, outils de backtest et MonitorAlgo
(Spring Boot/React). Ces composants sont distincts ; les sources ne qualifient ni
collecte live, ni Replay, ni broker ou résultat de trading.

Le développement reste sur [GitLab](https://gitlab.com/simonbdbc/motivewave-dev).
Un miroir GitHub filtré avec historique neuf est prévu ; sa publication et sa
synchronisation ne sont pas attestées. Les droits du code/SDK/bibliothèques et la
provenance des SQLite restent à clarifier ; aucune licence globale n'est ajoutée.

## Structure et prérequis

- `MotiveWave_Studies/src` : études/stratégies pour le SDK MotiveWave.
- `MotiveWave_Studies/PatternBacktest` : backtest Java distinct.
- `MotiveWave_Studies/PatternBacktestStrategy` : prototype absent du settings principal.
- `MonitorAlgo` : API REST/WebSocket et lecture de journaux, frontend React.

Le build des études déclare le compilateur Java 24. Les wrappers déclarent
Gradle 8.8. Le JAR du wrapper des études est présent ; celui du backend
MonitorAlgo est absent. La recette vérifiée
utilise une distribution Gradle 8.8 séparée sous JDK 21.
MonitorAlgo utilise Spring Boot 3.2.5 ; son
Dockerfile historique utilise JDK 17 et Node 18. Qualifier les builds séparément,
avec les lockfiles suivis et une distribution Gradle identifiée ; ne pas changer
les versions pour présenter un succès. Python 3 sert aux contrôles ciblés de configuration.

## Installation et configuration

```bash
git clone https://gitlab.com/simonbdbc/motivewave-dev.git
cd motivewave-dev
java -version
javac -version
python3 scripts/test-db-config.py
```

GitLab est actuellement privé et requiert un accès autorisé. Le test compile les
quatre chargeurs externes et quatre pools avec des données inventées. Il ne lance
ni PostgreSQL ni MotiveWave et ne qualifie pas l'application complète.

Les credentials PostgreSQL restent hors Git et hors JAR : copier le modèle
`db.properties_example` du composant dans un emplacement privé externe, mode
0600, puis passer son chemin via la propriété JVM ou variable dédiée.
[Configuration, variables et rotation](docs/SECURITY.md). Un mot de passe vide
est refusé. Ne jamais essayer le credential exposé pour tester sa validité.

## Build et démarrage des études

Après installation des dépendances et revue des droits SDK :

```bash
cd MotiveWave_Studies
# JAVA_HOME pointe vers un JDK 21 ; le toolchain Java 24 doit être installé.
# GRADLE_88_HOME pointe vers une distribution Gradle 8.8 vérifiée.
"$GRADLE_88_HOME/bin/gradle" --version
"$GRADLE_88_HOME/bin/gradle" --no-daemon classes
"$GRADLE_88_HOME/bin/gradle" --no-daemon jar
```

Le sous-projet BacktestDbInitializer référencé dans settings.gradle est absent ;
PatternBacktestStrategy comporte aussi une incohérence historique de package.
La recette processResources/jar des projets configurés a réussi le 10 octobre
2026 avec runtime 21 et compilateurs 24/21 selon le composant. Le prototype non
inclus reste à qualifier séparément. Le backend et le frontend MonitorAlgo ont
été compilés séparément le 11 octobre 2026 ; cela ne qualifie ni leur démarrage
complet ni MotiveWave/Replay/broker.
Les tâches deploy/deployJar/clean peuvent écrire ou supprimer des fichiers dans
MotiveWave Extensions de l'utilisateur : réserver ces commandes à une installation
séparée. Elles ne font pas partie de la recette de configuration.

L'exécution d'une étude exige une version compatible de MotiveWave et du SDK,
sa configuration privée et un jeu synthétique dédié. Charger une étude ou compiler
un JAR ne prouve pas l'exécution Replay/broker. Les huit SQLite historiques ne
sont pas des fixtures publiques par défaut.

## MonitorAlgo, persistance et arrêt

Voir [MonitorAlgo](MonitorAlgo/README.md) pour les deux bases et journaux.
PostgreSQL/SQLite et les journaux persistent en dehors des processus : les conserver
dans un stockage privé, jamais dans le candidat public. Arrêter Java/Gradle avec
Ctrl+C, les conteneurs avec docker compose down depuis MonitorAlgo ; cette stack
ne crée pas le PostgreSQL externe. Détacher les études via MotiveWave avant de
modifier leurs pools/configurations. Ne pas supprimer les bases pour arrêter.

Les performances MonitorAlgo exigent un benchmark reproductible avec données
fictives ; aucun maximum de volume ni nouveau chiffre n'est revendiqué.
