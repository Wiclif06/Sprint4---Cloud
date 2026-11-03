#!/usr/bin/env bash
set -euo pipefail
export MSYS_NO_PATHCONV=1
source "$(dirname "$0")/env.sh"

for name in ARTIFACT_DIR IMAGE_TAG SOURCE_COMMIT DB_URL DB_USERNAME DB_PASSWORD ADMIN_USERNAME ADMIN_PASSWORD; do
  value="${!name:-}"
  if [[ -z "$value" || "$value" == '$('* ]]; then
    echo "Variavel obrigatoria ausente: $name" >&2
    exit 1
  fi
done
if [[ "$DB_URL" != jdbc:postgresql://* ]]; then
  echo "DB_URL deve ser uma URL JDBC do PostgreSQL." >&2
  exit 1
fi

cd "$ARTIFACT_DIR"
sha256sum --check SHA256SUMS
ACR_SERVER=$(az acr show -g "$RESOURCE_GROUP" -n "$ACR_NAME" --query loginServer -o tsv)
IMAGE="$ACR_SERVER/$IMAGE_NAME:$IMAGE_TAG"
az acr login --name "$ACR_NAME" --output none
trap 'docker logout "$ACR_SERVER" >/dev/null 2>&1 || true' EXIT

docker build --build-arg SOURCE_COMMIT="$SOURCE_COMMIT" \
  --build-arg BUILD_ID="$IMAGE_TAG" -f Dockerfile.runtime -t "$IMAGE" .
docker push "$IMAGE"

ACR_IDENTITY=$(az identity show -g "$RESOURCE_GROUP" -n "$ACI_IDENTITY_NAME" --query id -o tsv)

az container create --resource-group "$RESOURCE_GROUP" --name "$ACI_API_NAME" \
  --location "$LOCATION" --image "$IMAGE" --registry-login-server "$ACR_SERVER" \
  --assign-identity "$ACR_IDENTITY" --acr-identity "$ACR_IDENTITY" \
  --os-type Linux --ip-address Public --dns-name-label "$ACI_API_DNS" \
  --ports 8080 --cpu 1 --memory 1.5 --restart-policy Always \
  --environment-variables MAIL_ENABLED=false BOOTSTRAP_ADMIN_ENABLED=true DB_CONNECTION_TIMEOUT=5000 \
  --secure-environment-variables DB_URL="$DB_URL" DB_USERNAME="$DB_USERNAME" \
    DB_PASSWORD="$DB_PASSWORD" ADMIN_USERNAME="$ADMIN_USERNAME" ADMIN_PASSWORD="$ADMIN_PASSWORD" \
  --output none

FQDN=$(az container show -g "$RESOURCE_GROUP" -n "$ACI_API_NAME" --query ipAddress.fqdn -o tsv)
test -n "$FQDN"
printf 'Aplicacao: http://%s:8080/swagger-ui.html\n' "$FQDN"
echo "##vso[task.setvariable variable=apiUrl]http://$FQDN:8080"

