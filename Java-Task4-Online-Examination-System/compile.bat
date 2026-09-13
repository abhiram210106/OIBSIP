@echo off
title Compile Online Examination System
color 0A

echo ========================================================
echo    COMPILING OASIS CBT - ONLINE EXAMINATION SYSTEM      
echo ========================================================
echo.

cd /d "%~dp0"

if not exist bin mkdir bin

echo [*] Cleaning previous class files...
del /q bin\com\oasis\exam\*.class 2>nul
del /q bin\com\oasis\exam\model\*.class 2>nul
del /q bin\com\oasis\exam\service\*.class 2>nul
del /q bin\com\oasis\exam\ui\*.class 2>nul
del /q bin\com\oasis\exam\ui\components\*.class 2>nul

echo [*] Compiling Java source files...
javac -encoding UTF-8 -d bin -sourcepath src src\com\oasis\exam\Main.java src\com\oasis\exam\model\*.java src\com\oasis\exam\service\*.java src\com\oasis\exam\ui\*.java src\com\oasis\exam\ui\components\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [X] Compilation failed with error code %ERRORLEVEL%.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [V] Compilation successful! All bytecode generated in \bin.
echo.
pause
