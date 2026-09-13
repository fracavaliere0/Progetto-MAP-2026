@echo off
setlocal DisableDelayedExpansion

if not "%~1"=="" (
    echo Uso: avvia-gui.bat
    echo Host e porta si impostano nella finestra della GUI.
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

if not exist "%~dp0gui\mapGUI.jar" (
    echo ERRORE: manca gui\mapGUI.jar. Estrarre tutta la distribuzione.
    goto :fail
)
rem La GUI cerca il dataset predefinito in distribution/src dalla radice.
pushd "%~dp0.."
if errorlevel 1 goto :fail

echo Avvio dell'interfaccia grafica. Lasciare aperta questa console.
echo La modalita' File locale non richiede server o MySQL.
echo Per la modalita' Database avviare prima avvia-server.bat.
"%JAVA_CMD%" -jar "distribution\gui\mapGUI.jar"
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
