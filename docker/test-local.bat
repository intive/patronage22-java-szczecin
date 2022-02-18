@echo off

docker-compose -f docker\docker-compose-local-db.yml down --remove-orphans
docker-compose -f docker\docker-compose-local-db.yml build
docker-compose -f docker\docker-compose-local-db.yml up -d
./mvnw clean verify
docker-compose -f docker\docker-compose-local-db.yml down --remove-orphans