# Progetto MAP 2026 — Alberi di regressione

Applicazione Java per apprendere alberi di regressione e svolgere predizioni
interattive, disponibile come client/server su MySQL e come applicazione locale
`src` su file.

## Struttura

```text
project/
  mapClient/       Client da console — progetto Eclipse
  mapServer/       Server TCP multiclient — progetto Eclipse
  src/             Applicazione locale — progetto Eclipse
distribution/     JAR eseguibili, driver JDBC, script SQL e dataset
docs/             Javadoc e diagrammi UML
tests/            Suite Maven con tre moduli indipendenti
```

## Avvio

Serve Java 8 o successivo; per il server serve anche MySQL.

Preparare il database con lo script fornito, usando un account amministrativo:

```sh
mysql -u root -p < distribution/server/sql_file/map6_setup.sql
```

Il server usa `localhost:3306/MapDB`, utente `MapUser`, password `map`.
Lo script è destinato alla prima installazione; non eseguirlo nuovamente su un
database già configurato senza verificarne il contenuto.

Avviare il server:

```sh
cd distribution/server
java -jar server.jar 8080
```

In un altro terminale, dalla radice del progetto, avviare il client:

```sh
java -jar distribution/client/client_base.jar localhost 8080
```

Per l'applicazione locale `src`, senza MySQL:

```sh
cd distribution/src
java -jar src.jar
```

Sono inclusi i dataset `prova`, `provaC` e `servo`. Inserire i nomi senza
estensione: l'applicazione `src` aggiunge `.dat` ai dataset e `.dmp` agli archivi.
Nel client inserire il nome della tabella per apprendere, oppure il nome
senza `.dmp` per caricare un albero. Il server salva gli archivi nella propria
directory di lavoro. Gli archivi di server e `src` non sono intercambiabili.

## Sorgenti ed Eclipse

Importare `project/` con **File → Import → Existing Projects into Workspace**,
abilitando **Search for nested projects**. Selezionare `mapClient`, `mapServer`
e `src`, senza copiare i progetti nel workspace; configurare `JavaSE-1.8`
con un JDK disponibile. Se erano già importati prima dello spostamento, rimuovere
solo i vecchi riferimenti dal workspace, senza cancellare i file, e reimportarli.

I punti di ingresso sono `map7Client.MainTest`, `Server.Server` e `MainTest`.
Le tre radici dei sorgenti vanno compilate separatamente. Il progetto server
collega il driver da `distribution/server/mysql-connector-java-8.0.17.jar`.
I JAR distribuiti sono separati dagli output di Eclipse e dei test: dopo modifiche
ai sorgenti devono essere nuovamente esportati.

## Test

Con Maven e un JDK che supporti `--release 8`, dalla radice:

```sh
mvn -f tests/pom.xml clean verify
```

I moduli isolano client, server e `src`. Le prove server usano un driver
JDBC simulato e non modificano MySQL. I report sono generati nelle directory
`tests/*/target/`. Per importarli in Eclipse usare **Existing Maven Projects**.

## Documentazione

- [Report tecnico](docs/ReportTecnico.pdf)
- [Manuale utente](docs/GuidaUtente.pdf)

I sorgenti LaTeX sono in `docs/latex/`.

Aprire [docs/index.html](docs/index.html) per consultare i Javadoc separati e i
[14 diagrammi UML](docs/uml/index.html), disponibili come sorgenti Mermaid e immagini PNG.
I diagrammi rappresentano le applicazioni, non i test.

I file `.mmd` sono modificabili con Mermaid; le immagini PNG corrispondenti
si trovano in `img/`.
