#!/usr/bin/env bash
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
SOURCE="$HERE/uml"
MERMAID_CLI="${MERMAID_CLI:-mmdc}"

if ! command -v "$MERMAID_CLI" >/dev/null 2>&1; then
    printf 'Mermaid CLI non trovato. Installare @mermaid-js/mermaid-cli (vedere docs/uml/README.md).\n' >&2
    exit 1
fi

EXTRA=()
if [[ -n "${PUPPETEER_CONFIG:-}" ]]; then
    if [[ ! -f "$PUPPETEER_CONFIG" ]]; then
        printf 'Configurazione Puppeteer non trovata: %s\n' "$PUPPETEER_CONFIG" >&2
        exit 1
    fi
    EXTRA+=(--puppeteerConfigFile "$PUPPETEER_CONFIG")
fi

WORK="$(mktemp -d "${TMPDIR:-/tmp}/map-uml.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT
mapfile -d '' -t DIAGRAMS < <(find "$SOURCE" -type f -name '*.mmd' -print0 | sort -z)
if (( ${#DIAGRAMS[@]} == 0 )); then
    printf 'Nessun diagramma Mermaid in %s\n' "$SOURCE" >&2
    exit 1
fi

for diagram in "${DIAGRAMS[@]}"; do
    relative="${diagram#"$SOURCE/"}"
    destination="${relative%/*}/img/${relative##*/}"
    destination="${destination%.mmd}"
    mkdir -p "$WORK/$(dirname "$destination")"
    printf 'Rendering: %s\n' "$relative"
    for format in svg png; do
        "$MERMAID_CLI" --input "$diagram" --output "$WORK/$destination.$format" \
            --configFile "$SOURCE/mermaid-config.json" \
            --backgroundColor white --width 1800 --scale 2 "${EXTRA[@]}"
    done
done

# Non sostituire le immagini esistenti se un diagramma non viene compilato.
for diagram in "${DIAGRAMS[@]}"; do
    relative="${diagram#"$SOURCE/"}"
    destination="${relative%/*}/img/${relative##*/}"
    destination="${destination%.mmd}"
    mkdir -p "$SOURCE/$(dirname "$destination")"
    cp "$WORK/$destination.svg" "$WORK/$destination.png" "$SOURCE/$(dirname "$destination")/"
done
printf 'Creati %s diagrammi in SVG e PNG. Indice: %s/index.html\n' "${#DIAGRAMS[@]}" "$SOURCE"
