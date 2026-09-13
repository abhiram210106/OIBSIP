@echo off
echo ==========================================================
echo Compiling Digital Library Management System (Task 5)...
echo ==========================================================

if not exist "bin" mkdir bin

javac -cp "lib/*" -d bin src/com/library/*.java src/com/library/models/*.java src/com/library/database/*.java src/com/library/dao/*.java src/com/library/server/*.java

if %errorlevel% equ 0 (
    echo [SUCCESS] Compilation completed without errors.
) else (
    echo [ERROR] Compilation failed.
)
pause
