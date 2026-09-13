# OASIS INFOBYTE - TASK 4: ONLINE EXAMINATION SYSTEM
# PowerShell Launcher Script

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $ScriptDir

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   OASIS INFOBYTE - TASK 4: ONLINE EXAMINATION SYSTEM   " -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[*] Compiling Java source files..." -ForegroundColor Green
$sources = Get-ChildItem -Recurse -Filter "*.java" "src" | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -d "bin" $sources

Write-Host "[*] Launching Online Examination System GUI..." -ForegroundColor Green
Start-Process "javaw" -ArgumentList "-cp bin com.oasis.exam.Main"
