#!/bin/sh

set -eu

if [ ! -s /app/keys/app.key ] || [ ! -s /app/keys/app.pub ]; then
    openssl genpkey -algorithm RSA -out /app/keys/app.key -pkeyopt rsa_keygen_bits:2048 >/dev/null 2>&1
    openssl rsa -in /app/keys/app.key -pubout -out /app/keys/app.pub >/dev/null 2>&1
fi

export RSA_PRIVATE_KEY="${RSA_PRIVATE_KEY:-file:/app/keys/app.key}"
export RSA_PUBLIC_KEY="${RSA_PUBLIC_KEY:-file:/app/keys/app.pub}"

exec java -jar /app/app.jar
