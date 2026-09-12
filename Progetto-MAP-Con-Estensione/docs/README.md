# Documentazione

- [Diagrammi UML](uml/index.html): 16 diagrammi di base ed estensione GUI,
  disponibili in Mermaid, SVG e PNG; [guida e rigenerazione](uml/README.md).
- [Report tecnico](report_tecnico.pdf): progetto base, architettura della GUI,
  training locale e remoto, protocollo, limiti e test.
- [Manuale utente](manuale_utente.pdf): avvio del server e della CLI, uso della
  GUI e formato dei dataset.

## Diagrammi UML

La raccolta in `uml/` riprende l’organizzazione di `../Progetto-MAP`,
adattandola alle classi effettive di questa estensione Swing. La GUI ha due
diagrammi: uno delle classi e uno dei package.
Restano le viste della base e la panoramica dell’architettura;
non sono presenti diagrammi di sequenza. La galleria HTML funziona anche offline.

Per rigenerare le immagini, con Mermaid CLI disponibile:

```bash
bash docs/build-uml.sh
```

Versione dello strumento, installazione e convenzioni sono descritte in
[`uml/README.md`](uml/README.md). La generazione UML è indipendente dai PDF.

## Sorgenti LaTeX e stile

I documenti riprendono i sorgenti di `Progetto-MAP/docs/latex/` del progetto
base. `latex/stile.sty` è copiato senza modifiche: stessi caratteri, colori,
intestazioni, margini, tabelle e blocchi di terminale. Le sezioni della base
sono conservate, con introduzioni e prerequisiti adattati all'estensione.

| File | Contenuto |
| --- | --- |
| `latex/ReportTecnico.tex` | Documento principale del report e sezioni della base |
| `latex/report-gui.tex` | Architettura, funzionamento, compilazione e limiti della GUI |
| `latex/report-test-gui.tex` | Test e risultati dell'estensione |
| `latex/GuidaUtente.tex` | Documento principale del manuale e istruzioni della CLI |
| `latex/manuale-gui.tex` | Istruzioni per la GUI |
| `latex/stile.sty` | Foglio di stile originale condiviso |
| `latex/immagini/` | Immagini dei componenti reali della GUI |

`gui-locale.png` mostra il risultato ottenuto da `prova.dat` con le classi
attuali, ricompilate per Java 8. `gui-database.png` mostra soltanto il pannello
di configurazione: non rappresenta un collegamento a MySQL verificato.

I sorgenti LaTeX e le immagini sono autosufficienti nella cartella `latex/`:
la compilazione non richiede il progetto base, Java o MySQL. Per Overleaf,
caricare il contenuto di questa cartella e scegliere come documento principale
`ReportTecnico.tex` oppure `GuidaUtente.tex`.

## Compilazione dei PDF

Serve **Tectonic** oppure una distribuzione LaTeX completa con XeLaTeX o
pdfLaTeX. Sono necessari i pacchetti richiamati da `stile.sty`, inclusi Babel
italiano e Latin Modern, e `graphicx` per le immagini. Tectonic scarica le
risorse mancanti al primo utilizzo; un ambiente offline deve averle già in cache.

Dalla cartella principale del progetto:

```bash
bash docs/build-docs.sh
```

Lo script cerca nell'ordine Tectonic, XeLaTeX e pdfLaTeX. Per scegliere un
motore o indicarne il percorso:

```bash
LATEX_ENGINE=tectonic bash docs/build-docs.sh
# oppure
LATEX_ENGINE=xelatex bash docs/build-docs.sh
```

I PDF vengono pubblicati come `docs/report_tecnico.pdf` e
`docs/manuale_utente.pdf` solo dopo la compilazione di entrambi. I file
intermedi vengono creati in una directory temporanea e rimossi all'uscita.
La compilazione verificata per questa revisione usa Tectonic 0.17.0.

## Verifiche documentate

Con OpenJDK 21 e compilazione per Java 8:

| Suite | Test JUnit | Fallimenti/errori | Saltati |
| --- | ---: | ---: | ---: |
| Server | 66 | 0 | 0 |
| Client da console | 30 | 0 | 0 |
| GUI | 22 | 0 | 0 |
| **Totale** | **118** | **0** | **0** |

Sono stati eseguiti anche i tre programmi autonomi
`DatabaseTreeClientTest`, `VisualTreeStatisticsTest` e
`DatabaseTreeIntegrationTest`, tutti con asserzioni abilitate e risultato
positivo. Non sono conteggiati come casi JUnit aggiuntivi.

Le prove remote usano server/socket locali e JDBC simulato, non MySQL reale.
La copertura JaCoCo riguarda solo i moduli base server e client; non è stata
misurata per la GUI. I limiti delle verifiche sono descritti nel report.

### Suite base e controlli autonomi GUI

Dalla cartella principale, con JDK e Maven installati:

```bash
mvn -f tests/server/pom.xml verify
mvn -f tests/client/pom.xml verify
bash estensioni/gui/build-gui.sh --check
```

I comandi sopra verificano separatamente server e client. Per eseguire anche
la suite dell'applicazione locale `src`, usare l'aggregatore dei tre moduli:

```bash
mvn -f tests/pom.xml clean verify
```

Il modulo `tests/src` compila i sorgenti di `project/src/src` e usa i dataset
in `distribution/src/`.

`build-gui.sh --check` ricrea `distribution/gui/mapGUI.jar` ed esegue i tre
programmi autonomi, **non** le tre classi JUnit della GUI.

### Suite JUnit della GUI

Le classi in `tests/estensioni/gui/` non sono collegate a un modulo Maven.
Dopo i comandi precedenti, JUnit 4.12 e Hamcrest 1.3 sono disponibili nella
cache Maven standard. In Bash su Linux/macOS, dalla cartella principale:

```bash
(
    set -euo pipefail
    CHECK="$(mktemp -d)"
    trap 'rm -rf "$CHECK"' EXIT
    JUNIT="$HOME/.m2/repository/junit/junit/4.12/junit-4.12.jar"
    HAMCREST="$HOME/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
    CP="distribution/gui/mapGUI.jar:$JUNIT:$HAMCREST"

    javac -encoding UTF-8 -cp "$CP" -d "$CHECK" \
        tests/support/TestData.java \
        estensioni/gui/VisualTreeStatisticsTest.java \
        tests/estensioni/gui/VisualTreeTest.java \
        tests/estensioni/gui/RegressionTreeCanvasTest.java \
        tests/estensioni/gui/RegressionTreeGUITest.java

    java -ea -Djava.awt.headless=false -cp "$CHECK:$CP" \
        org.junit.runner.JUnitCore \
        estensioni.gui.VisualTreeTest \
        estensioni.gui.RegressionTreeCanvasTest \
        estensioni.gui.RegressionTreeGUITest
)
```

Serve un display accessibile: i test creano finestre e dialoghi che chiudono
al termine. Il comando forza la modalità grafica per non saltare implicitamente
le verifiche della finestra; in un ambiente senza display occorre prima
predisporne uno, anche virtuale.

Se si esegue invece in modalità headless, i nove test di
`RegressionTreeGUITest` vengono saltati tramite assunzioni JUnit. In quel caso
la sola riga `OK (22 tests)` di `JUnitCore` non attesta che siano stati eseguiti
tutti: nella verifica riportata sono state controllate anche le assunzioni,
risultate zero.
