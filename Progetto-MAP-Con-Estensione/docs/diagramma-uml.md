# Diagrammi UML delle classi

Il progetto contiene due varianti eseguibili, rappresentate in due diagrammi per mantenere la lettura chiara:

1. **Versione locale e GUI** — [`SVG`](diagramma-classi.svg) · [`PDF`](diagramma-classi.pdf)<br>
   Sorgente: [`diagramma-classi.puml`](diagramma-classi.puml). Include tutte le classi di produzione in `src/`, `estensioni/gui/` e le dipendenze Java significative.<br>
2. **Client/server** — [`SVG`](diagramma-classi-client-server.svg) · [`PDF`](diagramma-classi-client-server.pdf)<br>
   Sorgente: [`diagramma-classi-client-server.puml`](diagramma-classi-client-server.puml). Include tutte le classi di produzione in `mapClient/src/` e `mapServer/src/`, compreso `TableData.QUERY_TYPE`.

Sono esclusi soltanto i test e le copie compilate/storiche. Le classi Java/SQL/Swing usate ma non definite nel progetto sono raggruppate nel package `Java API — tipi utilizzati`. I tipi primitivi e `String`/`Object` restano nelle firme per evitare nodi inutili.

## Notazione

- `+`, `#`, `~`, `-`: public, protected, package-private, private
- elemento sottolineato: attributo/metodo statico
- triangolo vuoto: generalizzazione o realizzazione
- rombo pieno: composizione
- rombo vuoto: aggregazione
- freccia tratteggiata: dipendenza
- `{leaf}`: classe finale; `⊕`: classe interna secondo Allen Holub
- `{frozen}`: attributo non modificabile dopo l'inizializzazione
- `<<interface>>`: interfaccia/template; `<<bind>> (Tipo)`: istanziazione esplicita di un template

## Rigenerazione

Con PlantUML e Graphviz installati:

```bash
plantuml -tsvg docs/diagramma-classi.puml docs/diagramma-classi-client-server.puml
plantuml -tpdf docs/diagramma-classi.puml docs/diagramma-classi-client-server.puml
```
