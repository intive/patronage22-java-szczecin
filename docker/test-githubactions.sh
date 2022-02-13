#!/bin/bash

docker-compose -f docker/docker-compose-test.yml down --remove-orphans
docker-compose -f docker/docker-compose-test.yml --env-file ./.env build
docker-compose -f docker/docker-compose-test.yml run --rm app
