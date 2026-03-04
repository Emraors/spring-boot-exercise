#!/usr/bin/env bash
set -euo pipefail

echo "Stopping development environment..."
docker compose down

echo "Done! All services stopped."

