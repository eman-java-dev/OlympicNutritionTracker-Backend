#!/bin/sh
set -eu

if [ -n "${MYSQL_TLS_TRUST_STORE_BASE64:-}" ]; then
    : "${MYSQL_TLS_TRUST_STORE_PASSWORD:?Set MYSQL_TLS_TRUST_STORE_PASSWORD for the MySQL truststore}"
    umask 077
    trust_dir=$(mktemp -d)
    trust_store="$trust_dir/mysql-ca.p12"
    if ! printf '%s' "$MYSQL_TLS_TRUST_STORE_BASE64" | base64 -d > "$trust_store" || [ ! -s "$trust_store" ]; then
        rm -f "$trust_store"
        rmdir "$trust_dir"
        echo "MYSQL_TLS_TRUST_STORE_BASE64 must contain a base64-encoded PKCS12 truststore" >&2
        exit 1
    fi
    export MYSQL_TLS_TRUST_STORE_URL="file:$trust_store"
    unset MYSQL_TLS_TRUST_STORE_BASE64
fi

exec "$@"
