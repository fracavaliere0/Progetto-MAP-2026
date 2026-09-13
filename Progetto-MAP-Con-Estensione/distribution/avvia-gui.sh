#!/bin/sh
set -eu

if [ "$#" -gt 0 ]; then
    echo 'Uso: sh avvia-gui.sh' >&2
    echo 'Host e porta si impostano nella finestra della GUI.' >&2
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

# La GUI cerca il dataset predefinito in distribution/src dalla radice.
cd "$SCRIPT_DIR/.."
if [ ! -f distribution/gui/mapGUI.jar ]; then
    echo 'ERRORE: manca distribution/gui/mapGUI.jar. Estrarre tutta la distribuzione.' >&2
    exit 1
fi

echo "Avvio dell'interfaccia grafica. Lasciare aperto questo terminale."
echo "La modalita' File locale non richiede server o MySQL."
echo "Per la modalita' Database avviare prima avvia-server.sh."
exec "$JAVA_CMD" -jar distribution/gui/mapGUI.jar
