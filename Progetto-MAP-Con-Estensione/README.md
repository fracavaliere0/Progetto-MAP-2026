# Progetto MAP 2026 — Alberi di regressione con estensione GUI

Applicazione Java per apprendere alberi di regressione e consultare predizioni.
Include client/server da console, applicazione locale su file e un'interfaccia
Swing per visualizzare l'albero, con training da file oppure da database.

## Struttura

```text
project/
  mapClient/       Client da console — progetto Eclipse
  mapServer/       Server TCP multiclient — progetto Eclipse
  src/             Applicazione locale — progetto Eclipse
estensioni/gui/    Sorgenti dell'interfaccia Swing
distribution/     Script di avvio .bat/.sh, JAR, driver JDBC, SQL e dataset
docs/             Manuale utente, report tecnico e diagrammi UML
tests/            Suite Maven della base e test separati della GUI
```

## Avvio dei JAR

Gli script eseguono **solo i JAR già presenti in `distribution/`**: non
compilano i sorgenti e non richiedono Eclipse o Maven. Estrarre tutto il progetto
in una cartella scrivibile e mantenere la struttura delle cartelle.

Serve **Java 8 o successivo**; per la GUI serve anche un ambiente desktop con
supporto Swing. Gli script usano `JAVA_HOME/bin/java` (su Windows `java.exe`);
se `JAVA_HOME` non è impostata, cercano `java` nel `PATH`.
Su Windows si può impostare `JAVA_HOME` nelle variabili di ambiente dell'account,
indicando la cartella del JDK senza virgolette e senza `\bin`.
Java configurato in Eclipse non è necessariamente disponibile agli script.

### Windows: prova della GUI senza MySQL

1. Aprire `distribution/` in Esplora file e fare doppio clic su **`avvia-gui.bat`**.
2. Lasciare selezionato **File locale**, con `distribution/src/prova.dat`.
3. Premere **Avvia training** per visualizzare l'albero.

Lo script imposta la radice del progetto come directory di lavoro, così il
percorso predefinito funziona anche avviandolo da un'altra cartella.
Per questa modalità non servono né server né MySQL. Lasciare aperta la console
associata alla GUI; chiudere la finestra grafica per terminare.

Per la versione locale da **console**, che consente anche di salvare e caricare
archivi `.dmp`, usare invece **`avvia-locale.bat`**. La GUI locale mantiene
l'albero soltanto in memoria e non crea archivi.

### Preparazione MySQL (solo per apprendere da database)

MySQL deve essere attivo sulla macchina del server. In **MySQL Workbench**,
collegarsi con un account amministrativo, aprire con **File → Open SQL Script**
il file `distribution/server/sql_file/map6_setup.sql` ed eseguirlo interamente.
In alternativa, dal Prompt dei comandi o da una shell Linux/macOS, dalla radice:

```sh
mysql -u root -p < distribution/server/sql_file/map6_setup.sql
```

Il server usa `localhost:3306/MapDB`, utente `MapUser`, password `map`.
Lo script SQL crea le tabelle `provaC` e `servo` ed è destinato alla prima
installazione: non rieseguirlo su un database già configurato senza verificarne
il contenuto. Gli script di avvio non installano né configurano MySQL.

### Windows: client/server e GUI da database

1. Fare doppio clic su **`distribution/avvia-server.bat`** e lasciare aperta la finestra.
2. Per la console, avviare **`distribution/avvia-client.bat`**: si collega a `localhost:8080`.
3. In alternativa, avviare **`distribution/avvia-gui.bat`**, selezionare **Database**
   e impostare tabella `provaC`, server `localhost` e porta `8080`.

Per cambiare porta o host della console, dal Prompt dei comandi nella radice,
eseguire in **due finestre separate**, prima il server e poi il client:

```bat
distribution\avvia-server.bat 9090
distribution\avvia-client.bat localhost 9090
```

Sostituire `localhost` con l'IP del server se remoto; usare la stessa porta
TCP (1–65535) per entrambi. **La GUI non accetta argomenti**: host e porta
si impostano nei campi della finestra. La porta del server MAP non è la `3306`
di MySQL. Per collegamenti remoti verificare rete e firewall.

In PowerShell anteporre `.\` ai percorsi degli script. Non aprire direttamente
i JAR con doppio clic: gli script configurano la directory di lavoro e lasciano
visibili gli errori. I `.bat` attendono un tasto al termine dell'applicazione.
Per fermare il server premere **Ctrl+C**, confermando se richiesto; la chiusura
della GUI o del client non arresta il server.

### Linux e macOS

Dalla radice del progetto, per la GUI locale:

```sh
sh distribution/avvia-gui.sh
```

Per usare il database, preparare MySQL e avviare prima il server:

```sh
sh distribution/avvia-server.sh
```

In un secondo terminale usare `sh distribution/avvia-gui.sh`, scegliendo
**Database**, oppure il client da console:

```sh
sh distribution/avvia-client.sh
```

Gli argomenti facoltativi sono gli stessi dei `.bat`: `[porta]` per il server,
`[host] [porta]` per il client. Senza argomenti si usano `localhost` e `8080`.
Per l'applicazione locale da console:

```sh
sh distribution/avvia-locale.sh
```

Gli script `.sh` non attendono un tasto al termine. Per interrompere il server
premere **Ctrl+C**.

## Dataset e archivi

Sono inclusi i dataset locali `prova.dat`, `provaC.dat` e `servo.dat` in
`distribution/src/`. Nella **GUI** scegliere il percorso del file completo di
estensione; nella **console locale** inserire il nome senza `.dat` o `.dmp`.
Nel client/server inserire il nome della tabella per apprendere, oppure il nome
senza `.dmp` per caricare un albero.

Gli script impostano automaticamente le directory di lavoro:

- il server salva gli archivi in `distribution/server`, anche quando il training
  è richiesto dalla GUI;
- la console locale legge dataset e salva archivi in `distribution/src`;
- la GUI parte dalla radice del progetto e, in modalità locale, non salva archivi.

Gli archivi di server e applicazione locale non sono intercambiabili.
La GUI non apre archivi `.dmp`: per riutilizzarli usare la console appropriata.

## Sorgenti ed Eclipse

Per eseguire i JAR non occorre importare nulla: usare gli script da Esplora file
o dal terminale. Solo per consultare o modificare i sorgenti della base,
importare `project/` con **File → Import → General → Existing Projects into
Workspace**, abilitando **Search for nested projects**. Selezionare `mapClient`,
`mapServer` e `src` senza copiare i progetti nel workspace; associare un JDK
compatibile a `JavaSE-1.8`. Il driver JDBC è collegato da `distribution/server/`.

Le tre radici dei sorgenti vanno compilate separatamente. La cartella
`estensioni/gui/` contiene i sorgenti Swing, ma non è un progetto Eclipse già
configurato: la GUI usa la base locale `project/src/src`, non le classi omonime
del server. Il JAR `distribution/gui/mapGUI.jar` contiene già quanto serve
all'esecuzione della GUI.

**Run As → Java Application** esegue le classi compilate dall'IDE, non i JAR.
Dopo modifiche ai sorgenti, gli script continuano a eseguire i JAR distribuiti
finché questi non vengono esportati nuovamente. La configurazione Java di
Eclipse è distinta da `JAVA_HOME` e `PATH` di Windows.

## Test e documentazione

Con Maven e un JDK che supporti `--release 8`, dalla radice:

```sh
mvn -f tests/pom.xml verify
```

La suite Maven comprende client, server e applicazione locale. I test server
usano un driver JDBC simulato e non modificano MySQL. I test GUI sono separati
e non vengono eseguiti da questo comando; le modalità di esecuzione sono
illustrate nel report tecnico.

- [Manuale utente](docs/manuale_utente.pdf), con istruzioni Windows e GUI
- [Report tecnico](docs/report_tecnico.pdf)
- [Diagrammi UML](docs/uml/README.md)

I sorgenti LaTeX sono in `docs/latex/`, incluse le sezioni del manuale dedicate
alla GUI in `manuale-gui.tex`.
