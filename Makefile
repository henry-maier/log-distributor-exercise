.PHONY: buildImages start stop clean

buildImages:
	./gradlew clean
	./gradlew build
	docker build -t distributor-image distributor
	docker build -t analyzer-image analyzer

start:
	docker-compose up

stop:
	docker-compose down

clean:
	@docker container prune -f
	@docker image prune -f
