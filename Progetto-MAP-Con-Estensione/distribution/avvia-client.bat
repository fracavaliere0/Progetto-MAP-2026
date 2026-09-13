@echo off
setlocal DisableDelayedExpansion

if not "%~3"=="" (
    echo Uso: avvia-client.bat [host] [porta]
    goto :fail
)
set "HOST=%~1"
set "PORTA=%~2"
if not defined HOST set "HOST=localhost"
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

if not exist "%~dp0client\client_base.jar" (
    echo ERRORE: manca client\client_base.jar. Estrarre tutta la distribuzione.
    goto :fail
)
pushd "%~dp0client"
if errorlevel 1 goto :fail

echo Avvio del client verso "%HOST%:%PORTA%". Il server deve essere gia' avviato.
"%JAVA_CMD%" -jar client_base.jar "%HOST%" "%PORTA%"
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
