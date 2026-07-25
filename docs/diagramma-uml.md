# Diagrammi UML delle classi

Il progetto offre due modalità separate, quindi il modello è diviso in due diagrammi leggibili:

1. **GUI locale** — [`SVG`](diagramma-classi.svg) · [`PDF`](diagramma-classi.pdf)  
   Sorgente: [`diagramma-classi.puml`](diagramma-classi.puml).  
   Comprende le classi di produzione in `src/` ed `estensioni/`.
2. **Client/server con database** — [`SVG`](diagramma-classi-client-server.svg) · [`PDF`](diagramma-classi-client-server.pdf)  
   Sorgente: [`diagramma-classi-client-server.puml`](diagramma-classi-client-server.puml).  
   Comprende le classi distribuite in `distribution/client/mapClient.jar` e `distribution/server/mapServer.jar`.

I test e la copia storica in `project/mapServer/src/` non sono inclusi.

## Collezioni Java e template

Seguendo l'indicazione ricevuta, i diagrammi non collegano direttamente il proprietario ai soli elementi: mostrano anche la struttura dati Java aggregata.

- Nella GUI, `VisualNode.children` aggrega `ArrayList<E>` con binding `E = VisualNode`; sono mostrate sia `List<E>` sia `ArrayList<E>` come template.
- Nel client/server sono mostrate esplicitamente `ArrayList<E>`, `LinkedList<E>`, `TreeSet<E>` e `HashMap<K,V>`, insieme alle interfacce template implementate.
- Ogni relazione riporta il binding effettivo, per esempio `E = Attribute` o `E = SplitInfo`.
- Le collezioni conservate nei campi sono aggregazioni; quelle create soltanto come variabili locali sono dipendenze `«create local»`.

## Notazione

- `+`: public
- `#`: protected
- `~`: visibilità di package
- `-`: private
- triangolo vuoto: generalizzazione o realizzazione
- rombo pieno: composizione
- rombo vuoto: aggregazione
- freccia tratteggiata: dipendenza

## Rigenerazione

Con PlantUML e Graphviz installati:

```bash
plantuml -tsvg docs/diagramma-classi.puml \
  docs/diagramma-classi-client-server.puml
plantuml -tpdf docs/diagramma-classi.puml \
  docs/diagramma-classi-client-server.puml
```

SVG e PDF sono vettoriali e possono essere ingranditi senza perdere qualità.
