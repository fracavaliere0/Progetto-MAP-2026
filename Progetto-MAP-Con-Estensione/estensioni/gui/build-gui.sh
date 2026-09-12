#!/usr/bin/env bash
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../.." && pwd)"
BASE="$ROOT/project/src/src"
JAR="$ROOT/distribution/gui/mapGUI.jar"
WORK="$(mktemp -d "${TMPDIR:-/tmp}/map-gui.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT

if javac --help 2>&1 | grep -q -- '--release'; then
    JAVA_LEVEL=(--release 8)
else
    JAVA_LEVEL=(-source 8 -target 8)
fi

mapfile -t SOURCES < <(
    find "$BASE/data" "$BASE/tree" "$BASE/utility" "$HERE" \
        -type f -name '*.java' ! -name '*Test.java' -print | sort
)
mkdir -p "$WORK/classes" "$(dirname "$JAR")"
javac "${JAVA_LEVEL[@]}" -encoding UTF-8 -d "$WORK/classes" "${SOURCES[@]}"
jar cfm "$JAR" "$HERE/MANIFEST.MF" -C "$WORK/classes" .

printf 'Creato %s\n' "$JAR"
printf 'Avvio: java -jar distribution/gui/mapGUI.jar\n'

if [[ "${1:-}" == "--check" ]]; then
    mkdir -p "$WORK/check"
    javac "${JAVA_LEVEL[@]}" -encoding UTF-8 -cp "$JAR" \
        -d "$WORK/check" "$HERE/DatabaseTreeClientTest.java" \
        "$HERE/VisualTreeStatisticsTest.java" \
        "$ROOT/tests/estensioni/gui/DatabaseTreeIntegrationTest.java"
    mapfile -t SERVER_SOURCES < <(
        find "$ROOT/project/mapServer/src" -type f -name '*.java' -print | sort
    )
    mkdir -p "$WORK/server"
    javac "${JAVA_LEVEL[@]}" -encoding UTF-8 -d "$WORK/server" \
        "${SERVER_SOURCES[@]}" "$ROOT/tests/server/src/com/mysql/cj/jdbc/Driver.java"
    (
        cd "$ROOT"
        java -ea -cp "$WORK/check:$JAR" estensioni.gui.DatabaseTreeClientTest
        java -ea -cp "$WORK/check:$JAR" estensioni.gui.VisualTreeStatisticsTest
        java -ea -cp "$WORK/check:$JAR" estensioni.gui.DatabaseTreeIntegrationTest \
            "$WORK/server" "$ROOT/tests/server/fixtures"
    )
    printf 'Verifiche GUI: OK\n'
fi
