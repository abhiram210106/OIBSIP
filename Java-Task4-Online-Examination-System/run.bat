@echo off
title OASIS CBT Online Examination System
color 0B

echo ========================================================
echo    OASIS INFOBYTE - TASK 4: ONLINE EXAMINATION SYSTEM   
echo ========================================================
echo.

cd /d "%~dp0"

if not exist bin mkdir bin

echo [*] Compiling Java source files...
javac -encoding UTF-8 -d bin -sourcepath src src\com\oasis\exam\Main.java src\com\oasis\exam\model\*.java src\com\oasis\exam\service\*.java src\com\oasis\exam\ui\*.java src\com\oasis\exam\ui\components\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [!] Compilation failed! Please check your JDK installation.
    pause
    exit /b %ERRORLEVEL%
)

echo [*] Launching Online Examination System GUI...
echo.
start javaw -cp bin com.oasis.exam.Main

exit
