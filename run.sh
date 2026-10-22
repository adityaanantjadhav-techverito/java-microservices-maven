#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"

mvn -q -DskipTests package

USER_PORT="${USER_SERVICE_PORT:-8081}"
ORDER_PORT="${ORDER_SERVICE_PORT:-8082}"
GATEWAY_PORT="${GATEWAY_PORT:-${PORT:-8080}}"
USER_URL="${USER_SERVICE_URL:-http://localhost:${USER_PORT}}"
ORDER_URL="${ORDER_SERVICE_URL:-http://localhost:${ORDER_PORT}}"

PORT="$USER_PORT" java -jar user-service/target/user-service.jar &
USER_PID=$!
PORT="$ORDER_PORT" java -jar order-service/target/order-service.jar &
ORDER_PID=$!

trap 'kill "$USER_PID" "$ORDER_PID" 2>/dev/null || true' EXIT INT TERM

PORT="$GATEWAY_PORT" USER_SERVICE_URL="$USER_URL" ORDER_SERVICE_URL="$ORDER_URL" \
    java -jar api-gateway/target/api-gateway.jar
