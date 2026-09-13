#!/bin/sh
set -eu

if [ "$#" -gt 0 ]; then
    echo 'Uso: sh avvia-locale.sh' >&2
    exit 1
fi
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

cd "$SCRIPT_DIR/src"
if [ ! -f src.jar ]; then
    echo 'ERRORE: manca distribution/src/src.jar. Estrarre tutta la distribuzione.' >&2
    exit 1
fi

echo "Avvio dell'applicazione locale, senza server e senza MySQL."
echo 'Dataset disponibili: prova, provaC, servo. Inserire il nome senza .dat.'
exec "$JAVA_CMD" -jar src.jar
