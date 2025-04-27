# Determine which Docker Compose command is available
DOCKER_COMPOSE := $(shell command -v docker compose 2> /dev/null || echo "docker compose")

# Default rule to set up the environment
all: install

install: 
	@docker run --rm -v ${PWD}:/var/www t-final-api.nameserver

clean:
	@echo "Clearing composer cache..."
	@docker run --rm -v ${PWD}:/var/www api-nameserver
	@docker run --rm -v ${PWD}:/var/www api-nameserver
	@docker run --rm -v ${PWD}:/var/www api-gateway
	@docker run --rm -v ${PWD}:/var/www api-gateway

start:
	@echo "Starting up containers for local development..."
	@$(DOCKER_COMPOSE) up --remove-orphans

destroy:
	@echo "Destroying containers... (including volumes)"
	docker compose down --volumes --remove-orphans
	docker compose rm -f
	docker rmi api-nameserver
	docker rmi api-gateway

.PHONY: all bootstrap install start populate destroy clean shell help