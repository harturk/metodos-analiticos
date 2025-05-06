ifeq ($(OS),Windows_NT)
	MVN := mvnw.cmd
else
	MVN := ./mvnw
endif

NAME ?= A

run:
	java -jar ./simulator.jar run $(NAME)


.PHONY: run