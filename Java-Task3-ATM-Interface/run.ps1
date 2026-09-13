# PowerShell Launcher for Oasis Infobyte Task 3: ATM Interface
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Clear-Host

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "  Compiling Oasis Infobyte Task 3: ATM Interface (Java)..." -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

javac -d bin src/*.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "`n[ERROR] Compilation failed! Please verify Java JDK installation." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "`n[SUCCESS] Compilation successful! Launching ATM Terminal...`n" -ForegroundColor Green
java -cp bin Main
