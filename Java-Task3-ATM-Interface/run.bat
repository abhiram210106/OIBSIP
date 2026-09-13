@echo off
title Oasis Global Bank - ATM Interface Simulation (Task 3)
chcp 65001 > nul
cls
echo ================================================================
echo   Compiling Oasis Infobyte Task 3: ATM Interface (Java)...
echo ================================================================
if not exist bin mkdir bin
javac -d bin src/*.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed! Please check that JDK 17+ is in your PATH.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [SUCCESS] Compilation complete. Launching ATM Terminal...
echo ================================================================
echo.
java -cp bin Main
pause
