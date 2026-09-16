#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/frontend"
npm ci
npm run build
rm -rf "$ROOT/backend/src/main/resources/static"
mkdir -p "$ROOT/backend/src/main/resources/static"
cp -R "$ROOT/frontend/dist/ptms/browser/." "$ROOT/backend/src/main/resources/static/"
cd "$ROOT/backend"
mvn clean verify
