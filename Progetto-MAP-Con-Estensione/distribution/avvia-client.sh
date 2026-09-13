#!/bin/sh
set -eu

if [ "$#" -gt 2 ]; then
    echo 'Uso: sh avvia-client.sh [host] [porta]' >&2
    exit 1
fi
HOST=${1:-localhost}
PORTA=${2:-8080}
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

if [ -n "${JAVA_HOME:-}" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
    if [ ! -x "$JAVA_CMD" ]; then
        echo 'ERRORE: JAVA_HOME non contiene bin/java eseguibile. Correggere JAVA_HOME.' >&2
        exit 1
    fi
else
    JAVA_CMD=java
    if ! command -v "$JAVA_CMD" >/dev/null 2>&1; then
        echo 'ERRORE: installare Java 8 o successivo e impostare JAVA_HOME oppure PATH.' >&2
        exit 1
    fi
fi

cd "$SCRIPT_DIR/client"
if [ ! -f client_base.jar ]; then
    echo 'ERRORE: manca distribution/client/client_base.jar. Estrarre tutta la distribuzione.' >&2
    exit 1
fi

printf "Avvio del client verso %s:%s. Il server deve essere gia' avviato.\n" "$HOST" "$PORTA"
exec "$JAVA_CMD" -jar client_base.jar "$HOST" "$PORTA"
