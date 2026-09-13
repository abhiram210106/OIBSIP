Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Compiling Digital Library Management System (Task 5)..." -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$sources = (Get-ChildItem -Path "src" -Recurse -Filter "*.java").FullName
javac -cp "lib/*" -d bin $sources

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] Compilation completed without errors." -ForegroundColor Green
} else {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
}
