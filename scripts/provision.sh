#!/usr/bin/env bash
set -euo pipefail
export MSYS_NO_PATHCONV=1
source "$(dirname "$0")/env.sh"

az account show --query "{subscription:name,id:id}" -o table
for provider in Microsoft.ContainerRegistry Microsoft.ContainerInstance Microsoft.DBforPostgreSQL Microsoft.ManagedIdentity; do
  az provider register --namespace "$provider" --wait
done
az group create --name "$RESOURCE_GROUP" --location "$LOCATION" --output none

if ! az acr show --resource-group "$RESOURCE_GROUP" --name "$ACR_NAME" --output none 2>/dev/null; then
  az acr create --resource-group "$RESOURCE_GROUP" --name "$ACR_NAME" \
    --location "$LOCATION" --sku Basic --admin-enabled false --output none
else
  az acr update --name "$ACR_NAME" --admin-enabled false --output none
fi

IDENTITY_PRINCIPAL=$(az identity create -g "$RESOURCE_GROUP" -n "$ACI_IDENTITY_NAME" \
  --location "$LOCATION" --query principalId -o tsv)
ACR_ID=$(az acr show -g "$RESOURCE_GROUP" -n "$ACR_NAME" --query id -o tsv)
az role assignment create --assignee-object-id "$IDENTITY_PRINCIPAL" \
  --assignee-principal-type ServicePrincipal --role AcrPull --scope "$ACR_ID" --output none

if ! az postgres flexible-server show -g "$RESOURCE_GROUP" -n "$PG_SERVER" --output none 2>/dev/null; then
  read -r -p "Administrador do PostgreSQL: " PG_USER
  read -r -s -p "Senha do PostgreSQL: " PG_PASSWORD
  printf '\n'
  az postgres flexible-server create --resource-group "$RESOURCE_GROUP" \
    --name "$PG_SERVER" --location "$PG_LOCATION" --admin-user "$PG_USER" \
    --admin-password "$PG_PASSWORD" --tier Burstable --sku-name Standard_B1ms \
    --storage-size 32 --version 16 \
    --public-access 0.0.0.0 --yes --output none
  unset PG_PASSWORD
else
  echo "Servidor existente: mantenha as credenciais cadastradas no Azure DevOps."
fi

az postgres flexible-server db create -g "$RESOURCE_GROUP" --server-name "$PG_SERVER" \
  --name "$PG_DB" --output none
az postgres flexible-server firewall-rule create -g "$RESOURCE_GROUP" -n "$PG_SERVER" \
  --rule-name allow-azure-services --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0 --output none

MY_IP=$(curl --fail --silent --show-error --connect-timeout 10 --max-time 20 https://api.ipify.org)
if [[ ! "$MY_IP" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Nao foi possivel identificar o IPv4 para a conexao SQL." >&2
  exit 1
fi
az postgres flexible-server firewall-rule create -g "$RESOURCE_GROUP" -n "$PG_SERVER" \
  --rule-name allow-developer-ip --start-ip-address "$MY_IP" --end-ip-address "$MY_IP" --output none

echo "Infraestrutura configurada."
printf 'DB_URL=jdbc:postgresql://%s.postgres.database.azure.com:5432/%s?sslmode=require\n' "$PG_SERVER" "$PG_DB"

