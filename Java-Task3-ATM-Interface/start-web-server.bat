@echo off
title Oasis Global Bank - ATM Web Server
chcp 65001 > nul
cls
echo ================================================================
echo   Launching Oasis Global Bank - Java ATM Web Server...
echo ================================================================
if not exist bin mkdir bin
javac -d bin src/*.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [OK] Starting Web Server at http://localhost:8080 ...
echo Browser will open automatically. Press Ctrl+C to stop.
echo ================================================================
echo.
java -cp bin ATMWebServer
pause
