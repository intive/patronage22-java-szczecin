@echo off

docker-compose -f docker\docker-compose-local-full.yml down --remove-orphans
docker-compose -f docker\docker-compose-local-full.yml build
docker-compose -f docker\docker-compose-local-full.yml --env-file %CD%\%CD%env up