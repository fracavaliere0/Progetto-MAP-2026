# Errori rilevati

## Aggiornamento dopo le correzioni

Sono stati corretti nel codice i seguenti punti segnalati in precedenza:

1. `RegressionTree.printRules()` e `printRules(String current)` aggiunti.
2. Calcolo dello `SSE` nel costruttore di `Node`.
3. Calcolo della media in `LeafNode.predictedClassValue`.
4. Indice `numberChild` corretto in `DiscreteNode.setSplitInfo()`.
5. Controllo limiti corretto in `Data.getExplanatoryAttribute()`.
6. `DiscreteAttribute.getNumberOfDistinctValues()` riallineato alla specifica.
7. `DiscreteNode.toString()` specializzato per l'output dei nodi discreti.
8. Formato di `LeafNode.toString()` corretto.
9. Nome degli attributi in output corretto tramite `Attribute.toString()`.

Restano aperti solo i problemi nelle parti fornite dal docente, l'assenza del dataset reale `servo.dat` e i problemi di Javadoc.

Nota: dove i PDF indicano codice "fornito dal docente", il problema viene comunque segnalato, ma non andrebbe corretto secondo la regola che hai dato.

## Verifiche eseguite

1. `javac src/*.java`
   Risultato attuale: compilazione completata con successo.
2. `java -cp src MainTest`
   Risultato: esecuzione verificata con un `servo.dat` temporaneo minimale; stampa di regole e albero corretta.
3. `java -cp src Data`
   Risultato sul repository attuale: `FileNotFoundException` per `servo.dat`, che non e' presente nel repository.

## Errori funzionali e di logica

1. File: `src/RegressionTree.java`
   Righe: metodo assente; richiamato in `src/MainTest.java:13`
   Problema: mancano `printRules()` e `printRules(String current)`, richiesti da `map2.pdf`.
   Effetto: il progetto non compila.
   Area docente: no.

2. File: `src/Node.java`
   Righe: `27-35`
   Problema: `variance` viene sempre impostata a `0.0` invece di essere calcolata come SSE del sottoinsieme coperto dal nodo.
   Specifica: `map2.pdf` richiede che il costruttore di `Node` calcoli la varianza del nodo.
   Effetto: tutte le varianze dell'albero sono errate; inoltre in `RegressionTree.determineBestSplitNode()` la scelta del miglior attributo degenera di fatto nel primo attributo con varianza minima trovata.
   Area docente: no.

3. File: `src/LeafNode.java`
   Righe: `16-22`
   Problema: `predictedClassValue` non viene mai calcolato come media dei valori dell'attributo di classe nella partizione.
   Specifica: `map2.pdf` richiede esplicitamente la media dei valori di classe nel range `beginExampleIndex...endExampleIndex`.
   Effetto: le foglie hanno classe predetta `null` e l'albero non puo' fare predizioni corrette.
   Area docente: no.

4. File: `src/DiscreteNode.java`
   Righe: `47` e `58`
   Problema: ogni `SplitInfo` viene creato con `numberChild = 0`.
   Specifica: ogni ramo deve mantenere il proprio indice figlio, come mostrato anche nell'output di esempio del PDF (`child 0`, `child 1`, ...).
   Effetto: in stampa tutti i rami risultano `child 0`, quindi la struttura dello split viene rappresentata in modo errato.
   Area docente: no.

5. File: `src/Data.java`
   Righe: `126-131`
   Problema: il controllo limiti usa `index > explanatorySet.length` invece di `index >= explanatorySet.length`.
   Effetto: con `index == explanatorySet.length` non scatta il controllo previsto e si arriva a un `ArrayIndexOutOfBoundsException` invece dell'eccezione gestita dal metodo.
   Area docente: no.

## Scostamenti dalla specifica

6. File: `src/DiscreteAttribute.java`
   Righe: `35-42`
   Problema: `getNumberOfDistinctValues()` usa un `HashSet` e restituisce il numero di valori unici, ma `map1.pdf` chiede la cardinalita' dell'array `values[]`.
   Effetto: il comportamento del metodo non coincide con la specifica.
   Area docente: no.

7. File: `src/DiscreteNode.java`
   Righe: metodo `toString()` assente
   Problema: `map2.pdf` richiede una specializzazione di `toString()` per i nodi discreti, ma la classe eredita la versione generica di `SplitNode`.
   Effetto: l'output non segue il formato richiesto `DISCRETE SPLIT : ...` mostrato negli esempi del PDF.
   Area docente: no.

8. File: `src/LeafNode.java`
   Righe: `49-50`
   Problema: il formato stampato e' `LEAF class=...` mentre negli esempi del PDF il formato richiesto e' `LEAF : class=...`.
   Effetto: scostamento dal formato di output richiesto.
   Area docente: no.

## Problemi in aree che i PDF indicano come fornite dal docente

9. File: `src/SplitNode.java`
   Righe: `181-185` e `192-199`
   Problema: `formulateQuery()` e `toString()` concatenano direttamente `attribute` invece di usare il nome dell'attributo.
   Effetto: l'output mostrera' valori del tipo `DiscreteAttribute@...` invece di nomi come `motor`, `screw`, `pgain`, `vgain`.
   Area docente: si, dal PDF la classe `SplitNode` risulta fornita dal docente.

10. File: `src/RegressionTree.java`
    Righe: `117-121`
    Problema: `printTree()` e' definito come `void` e stampa direttamente a video, mentre `map2.pdf` richiede `String printTree()`.
    Effetto: la firma del metodo non coincide con la specifica.
    Area docente: si, nel PDF il metodo risulta fornito dal docente.

11. Repository
    Problema: manca `servo.dat`, ma viene usato in `src/MainTest.java:9` e `src/Data.java:234`.
    Effetto: anche correggendo la compilazione, l'esecuzione fallisce subito con `FileNotFoundException`.
    Area docente: file di supporto mancante; non e' codice da correggere.

## Problemi di Javadoc

Stato attuale dopo la revisione:

1. Il Javadoc e' stato completato nelle classi principali, in particolare `Data` e `MainTest`.
2. I commenti semanticamente errati sono stati riallineati al comportamento reale del codice e alle specifiche del progetto.
3. Sono stati documentati anche i casi di errore principali, ad esempio le eccezioni di indice nei metodi di accesso di `Data`.

Controllo formale eseguito con:

1. `javadoc -Xdoclint:all -quiet -d /tmp/progetto-map-javadoc src/*.java`
   Risultato attuale: nessun warning.
2. `javadoc -private -Xdoclint:all -quiet -d /tmp/progetto-map-javadoc-private src/*.java`
   Risultato attuale: nessun warning.

Valutazione complessiva sul Javadoc: sistemato e ora formalmente pulito rispetto ai controlli `javadoc` e `doclint` eseguiti sul progetto.
