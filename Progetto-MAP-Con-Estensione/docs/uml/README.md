# Diagrammi UML — Progetto MAP con estensione

Aprire [index.html](index.html) nel browser: la galleria contiene **16 diagrammi**
con sorgenti Mermaid (`.mmd`) e immagini PNG. Le immagini
si consultano offline, senza Node.js o servizi esterni.
Per leggere le firme complete, aprire i PNG nelle dimensioni originali.

## Riferimenti e ambito

La raccolta riprende struttura della galleria, notazione e diagrammi della
base da `../../../Progetto-MAP/docs/uml/`, cioè `../Progetto-MAP/docs/uml/`
dalla radice di questo progetto. I 38 file Java applicativi della base sono
stati confrontati con la copia corrente e risultano identici. I 13 diagrammi
di dettaglio sono ripresi da questa raccolta; la panoramica è aggiornata per
includere la GUI.

I nuovi diagrammi descrivono le otto classi Swing effettive di
`estensioni/gui/` e le chiamate presenti nei sorgenti correnti.
La GUI ha **un diagramma delle classi e uno dei package**, senza viste separate
di presentazione, modello, client remoto o sequenze. Il diagramma
`client/gui/classi/mapClient.mmd` riunisce tutte le otto classi dell’estensione,
mantenendone campi e metodi.

La raccolta comprende 11 diagrammi delle classi, 4 viste dei package e una
panoramica architetturale, includendo anche l'applicazione locale `src` del progetto base.
Le viste dei package usano raggruppamenti e frecce dei flowchart Mermaid:
sono rappresentazioni architetturali, non diagrammi UML dei package con
notazione formale completa.

Sono coperti i **46 file Java applicativi**: 38 della base e 8 della GUI.
I test sono esclusi, compresi `DatabaseTreeClientTest.java` e
`VisualTreeStatisticsTest.java` accanto ai sorgenti dell’estensione.
`MainTest`, invece, è un punto di ingresso applicativo e viene incluso.

## Organizzazione

I percorsi nella tabella sono relativi a questa cartella.

| Cartella / file | Contenuto |
| --- | --- |
| `panoramica/progetto.mmd` | GUI, CLI, `src`, server, file e MySQL |
| `client/gui/package/estensioni_gui.mmd` | Package della GUI, JDK e dipendenze dalla base |
| `client/gui/classi/mapClient.mmd` | Tutte le otto classi GUI, campi, metodi e relazioni |
| `client/progettoBase/classi/` | Classi `map7Client` e `utility` della CLI (2 diagrammi) |
| `client/progettoBase/package/` | Package della CLI (1 diagramma) |
| `server/classi/` | Package `Server`, `data`, `database`, `tree` (4 diagrammi) |
| `server/package/` | Dipendenze fra package del server (1 diagramma) |
| `src/classi/` | `MainTest`, `data`, `tree`, `utility` locali (4 diagrammi) |
| `src/package/` | Package dell'applicazione locale `src` (1 diagramma) |

Ogni `.mmd` ha il corrispondente `img/<nome>.png` nella propria cartella. I commenti `%% Sorgente:` collegano i diagrammi ai file Java,
con percorsi relativi alla radice del progetto.

## Lettura e scelte di modellazione

- Visibilità: `+` pubblica, `-` privata, `#` protetta, `~` di package.
  Nel sorgente Mermaid `$` indica membri statici (sottolineati nell’immagine)
  e `*` metodi astratti (in corsivo). I generici usano `List~Tipo~`.
- Triangolo e linea continua: ereditarietà; triangolo e linea tratteggiata:
  realizzazione di un’interfaccia. Rombo pieno: composizione;
  freccia continua: associazione; freccia tratteggiata: dipendenza.
  Le molteplicità distinguono riferimenti opzionali, singoli e collezioni.
- Le classi principali di ogni vista riportano campi, costruttori e metodi
  dichiarati, inclusi quelli privati. Le classi esterne alla vista sono
  richiami di contesto, non nuove implementazioni; non si espandono i membri
  ereditati o tutti i tipi del JDK. Le clausole `throws` non sono elencate.
  `final` sulle classi GUI è indicato come annotazione `«final»`.
  In `VisualNode`, titolo, metadati, ramo, indicatore di foglia e riferimento
  alla lista dei figli sono final, ma lista e coordinate restano mutabili.
- Solo `RegressionTreeGUI` è pubblica fra le otto classi dell’estensione;
  tutte sono `final`. Presentazione, modello e rete appartengono allo stesso
  package `estensioni.gui`: i gruppi funzionali non sono sottopackage.
- La base locale si trova in **`project/src/src/`** in entrambi i progetti.
  I diagrammi corrispondenti si trovano in `docs/uml/src/`.
- `data` e `tree` del server e dell'applicazione `src` sono implementazioni distinte
  con classi omonime, da compilare separatamente. `mapGUI.jar` include solo
  la base locale; non include `MainTest` della CLI né le classi server.
- `RegressionTreeAccess` legge per riflessione `root` e `childTree` della base
  locale, senza aggiungere getter o modificare il modello. L’array dei figli
  è copiato. `VisualTree` e `VisualNode` sono la rappresentazione separata
  condivisa dai due percorsi di training. Le composizioni dei nodi descrivono
  la struttura ad albero costruita dalla GUI, non un vincolo imposto dalla
  mutabilità delle liste Java.
- Il canvas mantiene un’associazione opzionale al `VisualTree`; il risultato
  remoto condivide lo stesso albero, quindi non è modellato come proprietario
  esclusivo. `DatabaseTreeClient` possiede socket e stream della sessione.
- La GUI parla con il server MAP, **non direttamente con MySQL**. Usa i comandi
  `0`, `1`, `3`; ricostruisce l’albero ripetendo predizioni dalla radice e
  consumando sempre le risposte fino alla foglia. Il comando `2` resta della
  CLI. Il server salva il `.dmp`, ma non lo trasferisce alla GUI.
- Training e rete sono sincroni sull’EDT Swing; il centraggio è accodato con
  `invokeLater`. Il limite remoto è 10.000 nodi; i timeout di 30 secondi sono
  per connessione e singola lettura, non per l’intera elaborazione. I flussi
  di training e la gestione degli errori sono descritti nel
  [report tecnico](../report_tecnico.pdf), senza aggiungere diagrammi di sequenza.

## Aggiornamento dei diagrammi

I `.mmd` sono mantenuti come documentazione del codice. Quando si modifica un
diagramma, aggiornare anche la corrispondente immagine PNG. Se si aggiunge o
rinomina un diagramma, aggiornare anche `index.html` e questa guida.
