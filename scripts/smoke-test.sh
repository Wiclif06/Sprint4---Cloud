#!/usr/bin/env bash
set -euo pipefail
API_URL="${1:?Informe a URL da API}"
BODY=$(mktemp)
trap 'rm -f "$BODY"' EXIT

for attempt in $(seq 1 40); do
  if curl --fail --silent --show-error --connect-timeout 5 --max-time 10 \
      "$API_URL/actuator/health" -o "$BODY" 2>/dev/null \
      && jq -e '.status == "UP"' "$BODY" >/dev/null; then
    curl --fail --silent --show-error --connect-timeout 5 --max-time 20 \
      "$API_URL/api-docs" -o "$BODY"
    jq -e '.openapi and .paths["/clinics"] and .paths["/veterinarians"]' "$BODY" >/dev/null
    echo "API, PostgreSQL e OpenAPI disponiveis."
    exit 0
  fi
  printf 'Aguardando inicializacao: tentativa %s/40\n' "$attempt"
  sleep 10
done
echo "Falha: API ou PostgreSQL indisponivel." >&2
exit 1

