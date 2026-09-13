Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Starting Digital Library Management System (Task 5)..." -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    Write-Host "Compiling first..." -ForegroundColor Yellow
    & .\compile.ps1
}

Start-Process "http://localhost:8080"
java -cp "bin;lib/*" com.library.Main 8080
