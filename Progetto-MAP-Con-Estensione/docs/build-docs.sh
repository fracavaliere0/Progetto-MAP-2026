#!/usr/bin/env bash
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
WORK="$(mktemp -d "${TMPDIR:-/tmp}/map-latex.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT

# LATEX_ENGINE può contenere anche il percorso assoluto dell'eseguibile.
if [[ -z "${LATEX_ENGINE:-}" ]]; then
    for candidate in tectonic xelatex pdflatex; do
        if command -v "$candidate" >/dev/null 2>&1; then
            LATEX_ENGINE="$candidate"
            break
        fi
    done
fi
if [[ -z "${LATEX_ENGINE:-}" ]]; then
    printf 'Installare Tectonic o una distribuzione LaTeX completa.\n' >&2
    exit 1
fi

cd "$HERE/latex"
for document in ReportTecnico GuidaUtente; do
    case "${LATEX_ENGINE##*/}" in
        tectonic)
            "$LATEX_ENGINE" --outdir "$WORK" "$document.tex"
            ;;
        xelatex|pdflatex)
            # Due passaggi risolvono i riferimenti interni e i segnalibri.
            for pass in 1 2; do
                "$LATEX_ENGINE" -interaction=nonstopmode -halt-on-error \
                    -file-line-error -output-directory "$WORK" "$document.tex"
            done
            ;;
        *)
            printf 'Motore non supportato: %s (usare tectonic, xelatex o pdflatex).\n' "$LATEX_ENGINE" >&2
            exit 1
            ;;
    esac
done

# Pubblica i documenti soltanto dopo la compilazione di entrambi.
cp "$WORK/ReportTecnico.pdf" "$HERE/report_tecnico.pdf"
cp "$WORK/GuidaUtente.pdf" "$HERE/manuale_utente.pdf"
printf 'Creati:\n  %s\n  %s\n' "$HERE/report_tecnico.pdf" "$HERE/manuale_utente.pdf"
