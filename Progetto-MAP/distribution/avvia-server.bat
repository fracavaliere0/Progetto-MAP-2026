@echo off
setlocal DisableDelayedExpansion

if not "%~2"=="" (
    echo Uso: avvia-server.bat [porta]
    goto :fail
)
set "PORTA=%~1"
if not defined PORTA set "PORTA=8080"

if defined JAVA_HOME (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    if not exist "%JAVA_HOME%\bin\java.exe" (
        echo ERRORE: JAVA_HOME non contiene bin\java.exe. Correggere JAVA_HOME.
        goto :fail
    )
) else (
    set "JAVA_CMD=java.exe"
    where java.exe >nul 2>&1
    if errorlevel 1 (
        echo ERRORE: Java non trovato. Installare Java 8 o successivo e
        echo impostare JAVA_HOME oppure aggiungere la cartella bin al PATH.
        goto :fail
    )
)

if not exist "%~dp0server\server.jar" (
    echo ERRORE: manca server\server.jar. Estrarre tutta la distribuzione.
    goto :fail
)
if not exist "%~dp0server\mysql-connector-java-8.0.17.jar" (
    echo ERRORE: manca server\mysql-connector-java-8.0.17.jar.
    goto :fail
)
pushd "%~dp0server"
if errorlevel 1 goto :fail

echo Avvio del server sulla porta "%PORTA%". Arresto: Ctrl+C.
echo Se non compaiono errori, lasciare aperta questa finestra e avviare il client.
"%JAVA_CMD%" -jar server.jar "%PORTA%"
set "EXIT_CODE=%ERRORLEVEL%"
popd
goto :end

:fail
set "EXIT_CODE=1"
:end
echo.
echo Esecuzione terminata. Controllare gli eventuali messaggi sopra.
pause
exit /b %EXIT_CODE%
