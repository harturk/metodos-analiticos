# Determine which Docker Compose command is available
DOCKER_COMPOSE := $(shell command -v docker compose 2> /dev/null || echo "docker compose")

# Default rule to set up the environment
all: install start

install: 
	mvn clean package

start:
	java -jar target/metodos-t1-1.0-SNAPSHOT-jar-with-dependencies.jar ./modelo.yaml


.PHONY: all bootstrap install start populate destroy clean shell help