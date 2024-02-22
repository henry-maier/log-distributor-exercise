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

load:
	@jmeter -n -t jmeter/test-distributor.jmx -Jloops=100000 -Jusers=10

steadyLoad:
	jmeter -n -t jmeter/test-distributor-steadyload.jmx -Jthroughput=100000

format:
	./gradlew spotlessApply

run-analyzer:
	./gradlew analyzer:bootRun

run-distributor:
	./gradlew distributor:bootRun
