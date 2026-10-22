# 26-java-microservices-maven
Three runnable microservices + a shared library, in one Maven multi-module repo (plain JDK, no frameworks).

    shared-lib      library (no main)
    user-service    :8081  /users
    order-service   :8082  /orders
    api-gateway     :8080  /users, /orders (proxies), /health

Build:  mvn package     (fat jars in each module's target/)
Run:    sh run.sh       (builds, then starts all three services from the repo root)
Docker: mvn package && docker compose up --build
The root run target is `sh run.sh`; the gateway is available on :8080.
