#!/usr/bin/env bash
set -euo pipefail

echo "Starting development environment..."
docker compose up -d

echo "Waiting for postgres to be healthy..."
until docker compose ps postgres | grep -q "healthy"; do
  sleep 2
done

echo "Done! Services are running."
docker compose ps

