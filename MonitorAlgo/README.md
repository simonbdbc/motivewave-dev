# MonitorAlgo

Outil historique de lecture/analyse de journaux : backend Spring Boot 3.2.5,
frontend React. Distinct des études MotiveWave ; performances et déploiement non
qualifiés par cette préparation.

## Configuration et démarrage isolé

Le fichier est docker-compose.yaml : il construit le backend et ne fournit pas
PostgreSQL. Préparer deux bases dans une instance de développement dédiée et des
journaux fictifs. Aucune donnée de production n'est nécessaire.

```bash
cp .env.example .env
# Compléter les credentials privés et chemins de recette.
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build
```

Ne pas afficher le rendu complet Compose, qui peut contenir les credentials.
En conteneur, 127.0.0.1 désigne le conteneur : remplacer les URLs fictives par
celles de la base isolée accessible depuis ce réseau. Adapter UID/GID aux volumes.

- APP_DATASOURCE_URL/USERNAME/PASSWORD : base applicative.
- MW_DATASOURCE_URL/USERNAME/PASSWORD : base des statistiques.
- SPRING_DATASOURCE_* : alias explicite de compatibilité si un groupe est absent.
- DB_POOL_MAX/MIN/IDLE_TIMEOUT/CONNECTION_TIMEOUT : 20/2/60000/30000 par défaut.
- STRATEGY_LOG_FOLDER/MOTIVEWAVE_LOG_FOLDER/ALGO_LOG_FOLDER : journaux synthétiques.

Aucun mot de passe de secours. .env reste privée ; .env.docker n'est plus montée.
Retirer les anciennes copies après rotation. Le groupe Docker donne des droits
importants sur l'hôte et n'est pas nécessaire pour compiler les chargeurs.

## Vérification et arrêt

Depuis la racine : python3 scripts/test-db-config.py. Ce contrôle ne se connecte
à aucune base. Le 11 octobre 2026, le frontend (`npm ci --ignore-scripts`,
`npm run build-docker`) et le backend (`gradle --no-daemon build`, Gradle 8.8
externe sous JDK 21) ont été compilés séparément. Le wrapper backend suivi
manque son `gradle-wrapper.jar` ; utiliser une distribution Gradle 8.8 vérifiée
plutôt que présenter ce wrapper incomplet comme exécutable. Le backend ne contient
pas de tests métier (`test NO-SOURCE`).

Les quatre classes de pools réelles ont également été compilées sous Java 24
puis connectées à PostgreSQL 17.11 jetable : requête SELECT 1, valeurs par défaut
et fermeture réussies avec identifiants inventés. Cette recette utilise les JAR
locaux HikariCP 5.1.0 et JDBC 42.7.3 ; elle ne teste ni routes HTTP, schéma métier,
analyse de journaux, MotiveWave ou broker. La stack complète reste à qualifier.

Le Dockerfile historique utilise Node 18/JDK 17 ; ne pas le confondre
avec le build Java 24 des études. Arrêter avec docker compose down : journaux de
l'hôte et bases externes persistent. Examiner les erreurs dans le stockage privé,
puis consigner uniquement les résultats expurgés.

Voir [sécurité et rotation](../docs/SECURITY.md). Droits de redistribution à
clarifier ; un futur miroir GitHub filtré est prévu et n'est pas opérationnel.
