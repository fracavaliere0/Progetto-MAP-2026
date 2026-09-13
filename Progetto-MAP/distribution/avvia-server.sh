#!/bin/sh
set -eu

if [ "$#" -gt 1 ]; then
    echo 'Uso: sh avvia-server.sh [porta]' >&2
    exit 1
fi
PORTA=${1:-8080}
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

cd "$SCRIPT_DIR/server"
if [ ! -f server.jar ] || [ ! -f mysql-connector-java-8.0.17.jar ]; then
    echo 'ERRORE: mancano server.jar o mysql-connector-java-8.0.17.jar in distribution/server.' >&2
    exit 1
fi

printf 'Avvio del server sulla porta %s. Arresto: Ctrl+C.\n' "$PORTA"
echo 'Se non compaiono errori, lasciare aperto questo terminale e avviare il client.'
exec "$JAVA_CMD" -jar server.jar "$PORTA"
