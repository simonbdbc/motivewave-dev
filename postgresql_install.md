# PostgreSQL — recette et configuration privée

Le credential partagé anciennement suivi doit être renouvelé dans chaque instance
qui l'a utilisé. Supprimer les fichiers ne révoque pas le compte : voir la procédure
complète de [rotation](docs/SECURITY.md). Aucune opération de production sans
autorisation applicable, consommateurs identifiés et restauration vérifiée.

## Recette synthétique

Installer PostgreSQL selon le système et relever psql --version. Préparer une
instance jetable distincte. Créer des rôles/bases de démonstration adaptés au schéma
nécessaire et fournir un secret neuf par le gestionnaire de secrets ou la commande
interactive de changement de mot de passe de psql. Ne pas mettre un mot de passe
dans la ligne de commande, du SQL suivi ou une capture de terminal.

Les db.properties_example documentent les clés des études/backtests ; les copier
hors du dépôt et passer le chemin au chargeur du composant. MonitorAlgo utilise
APP_DATASOURCE_* et MW_DATASOURCE_* dans .env privée. Ces modèles n'initialisent
pas les tables ou données. Ne pas utiliser les anciennes SQLite comme démo publique.

Limiter listen_addresses et pg_hba.conf aux interfaces, bases, rôles et sources
nécessaires ; utiliser SCRAM et TLS lorsque la connexion sort de l'hôte. Ne pas
prescrire un accès global 0.0.0.0/0 pour simplifier l'installation.

## Validation et arrêt

Vérifier une connexion avec le secret neuf et le parcours applicatif synthétique.
Ne jamais essayer un secret exposé pour tester sa validité. Sauvegarder/restaurer
uniquement la recette pour valider la procédure. Arrêter l'instance par son
orchestration après vérification de son identité ; ne pas supprimer une base pour
arrêter l'application.

python3 scripts/test-db-config.py contrôle l'injection sans connexion DB. Il ne
remplace ni restauration, ni Hikari/SQL, ni confirmation de rotation effective.
