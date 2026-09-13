@echo off
echo ==========================================================
echo Starting Digital Library Management System (Task 5)...
echo ==========================================================

if not exist "bin" (
    echo Compiling first...
    call compile.bat
)

echo Launching Java Server on http://localhost:8080 ...
start http://localhost:8080
java -cp "bin;lib/*" com.library.Main 8080
pause
