@echo off
title IRCTC Online Railway Reservation System Launcher
echo =======================================================
echo    IRCTC Online Railway Reservation System Launcher
echo =======================================================
echo.
echo Compiling Java source files...
javac -cp "lib/*;src" -d bin src/App.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed! Please check your Java installation.
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful! Launching IRCTC Portal...
echo.
java -cp "lib/*;bin" App

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application closed with exit code %ERRORLEVEL%.
    pause
)
