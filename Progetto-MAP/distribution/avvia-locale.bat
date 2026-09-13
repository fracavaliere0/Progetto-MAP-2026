@echo off
setlocal DisableDelayedExpansion

if not "%~1"=="" (
    echo Uso: avvia-locale.bat
    goto :fail
)

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

if not exist "%~dp0src\src.jar" (
    echo ERRORE: manca src\src.jar. Estrarre tutta la distribuzione.
    goto :fail
)
pushd "%~dp0src"
if errorlevel 1 goto :fail

echo Avvio dell'applicazione locale, senza server e senza MySQL.
echo Dataset disponibili: prova, provaC, servo. Inserire il nome senza .dat.
"%JAVA_CMD%" -jar src.jar
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
