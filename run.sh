#!/usr/bin/env bash
cd "$(dirname "$0")"

(cd backend && ./mvnw spring-boot:run) &
(cd frontend && bun run dev 2>/dev/null || npm run dev) &
wait
