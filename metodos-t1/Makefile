ifeq ($(OS),Windows_NT)
	MVN := mvnw.cmd
else
	MVN := ./mvnw
endif

all: install start

install:
	$(MVN) clean package

start:
	java -jar target/metodos-t1-1.0-SNAPSHOT-jar-with-dependencies.jar ./modelo.yaml


.PHONY: all install start