# PostgreSQL, données et publication

Le mot de passe partagé anciennement suivi doit être considéré comme exposé.
Son retrait des fichiers ne révoque aucun droit dans PostgreSQL, aucun ancien
clone ni les anciennes ressources/JAR déjà installées.

## Configuration externe

Les chargeurs `DbConfig` conservent `get(String)` et les clés `db.url`, `db.user`,
`db.password`, `db.pool.max/min/idleTimeout/connectionTimeout`. Copier un fichier
`db.properties_example` dans un emplacement privé hors du checkout et lui donner
le mode 0600. Renseigner les valeurs privées par un canal sûr ; l'exemple laisse
le mot de passe vide et ne démarre donc pas sans configuration réelle.

| Composant | Propriété JVM (prioritaire) | Variable de chemin |
|---|---|---|
| Étude strategy | `mw.strategy.dbConfig` | `MW_STRATEGY_DB_CONFIG` |
| Étude pattern_strategy_algo | `mw.pattern.dbConfig` | `MW_PATTERN_DB_CONFIG` |
| PatternBacktest | `backtest.dbConfig` | `BACKTEST_DB_CONFIG` |
| PatternBacktestStrategy | `matcher.dbConfig` | `MATCHER_DB_CONFIG` |

Chaque composant conserve son propre fichier et sa propre base. Les ressources
classpath ne servent plus de secours. Un chemin absent, un fichier absent ou un
credential vide sont refusés. Les paramètres de pool ont les mêmes défauts :
20 connexions maximum, 2 minimum, idleTimeout 60000, connectionTimeout 30000 ms.

Les pools Java MonitorAlgo exigent `APP_DATASOURCE_URL/USERNAME/PASSWORD`
(base applicative) et `MW_DATASOURCE_URL/USERNAME/PASSWORD` (base des statistiques).
`SPRING_DATASOURCE_*` reste accepté comme configuration explicite de compatibilité.
Les deux copies des pools dans les études utilisent `SPRING_DATASOURCE_*`.
Aucun host ou mot de passe distant par défaut n'est conservé dans ces classes.

Compose charge la `.env` privée ; copier `MonitorAlgo/.env.example`, compléter
les chemins des journaux synthétiques et les credentials du PostgreSQL de
développement. Le montage de `.env.docker` n'est plus requis. Un chemin relatif
de journaux est relatif au dossier Compose ; ne pas monter les journaux réels en
recette. Les fichiers privés sont ignorés et `db.properties` est exclu des
ressources/JAR par Gradle, y compris pour les sous-projets.

## Rotation de production à faire autoriser

1. Le propriétaire identifie chaque instance ayant utilisé le credential :
   rôle PostgreSQL partagé, bases des études/backtests, `monitor_algo` et
   `strategy_stats`. Les configurations source ne prouvent pas quels services
   sont actifs. Recenser aussi tâches planifiées, CI, anciens JAR et clones.
2. Avant changement, relever seulement consommateurs, références de secrets,
   droits du rôle et ACL réseau ; sauvegarder base/config dans le stockage
   privé habituel. Tester une restauration dans une instance isolée.
3. Préparer un credential neuf dans le gestionnaire de secrets. Pour une coupure
   coordonnée : arrêter les consommateurs, modifier le rôle avec `psql \password`
   depuis un accès administrateur autorisé, distribuer le nouveau secret dans
   les fichiers externes/env privés et relancer progressivement. Ne pas passer
   le mot de passe sur la ligne de commande ou dans un script/log SQL.
4. Vérifier les consommateurs avec le nouveau secret seulement : connexion,
   tables attendues et flux applicatif synthétique autorisé. Ne jamais essayer
   l'ancien secret exposé pour « voir s'il fonctionne ».
5. Terminer les anciennes sessions du rôle après revue de leur usage. Retirer les
   copies dans les anciens dossiers Extensions et recompiler sans configuration
   privée. Relever SHA et ressources des nouveaux JAR. Restreindre écoute et
   `pg_hba.conf` aux réseaux réellement nécessaires, avec connexion chiffrée.
6. Si un consommateur échoue, le laisser arrêté, corriger son injection et
   recommencer sa vérification. Ne pas remettre le credential exposé. En cas de
   besoin de restauration, utiliser la sauvegarde vérifiée avec un credential
   neuf et une procédure approuvée ; consigner les effets sur les données.

Clôturer la rotation exige une preuve du changement effectif des comptes et des
consommateurs. Cette préparation ne modifie aucune instance, session ou secret
de production. Les instances et leur propriétaire restent à confirmer.

## Vérifications et limites

`python3 scripts/test-db-config.py` compile les quatre chargeurs et quatre pools
avec Java 24 ; vérifie les configurations externes fictives et le refus de
configuration absente avant connexion. Il ne se connecte à aucune base.

Les huit SQLite suivies sont des données de provenance non établie. Elles ne
sont pas des fixtures synthétiques par défaut. Exclure ces fichiers de l'export
public ; toute démo dépendante exige un schéma et un jeu fictif de remplacement.
Les JAR SDK et autres bibliothèques exigent une revue des droits et notices.

La compilation ne qualifie ni collecte live, ni MotiveWave, ni Replay, ni broker.
Les problèmes historiques de packages Java et de sous-projets Gradle restent
distincts du retrait des credentials ; une validation bloquée reste bloquée.
