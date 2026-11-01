@echo off
setlocal
set "DIR=%~dp0"
where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

if exist "%DIR%.mvn\apache-maven\bin\mvn.cmd" (
    call "%DIR%.mvn\apache-maven\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

echo Maven not found on PATH. Downloading portable Maven wrapper...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; $dest = '%DIR%.mvn\maven.zip'; if (-not (Test-Path '%DIR%.mvn')) { New-Item -ItemType Directory -Path '%DIR%.mvn' | Out-Null }; Write-Host 'Downloading Apache Maven 3.9.6...'; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile $dest; Write-Host 'Extracting...'; Expand-Archive -Path $dest -DestinationPath '%DIR%.mvn' -Force; Rename-Item -Path '%DIR%.mvn\apache-maven-3.9.6' -NewName 'apache-maven' -Force; Remove-Item $dest -Force -ErrorAction SilentlyContinue; Write-Host 'Maven installed successfully!'"

if exist "%DIR%.mvn\apache-maven\bin\mvn.cmd" (
    call "%DIR%.mvn\apache-maven\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
) else (
    echo Failed to initialize Maven. Please install Maven or use VS Code's Run button.
    exit /b 1
)
