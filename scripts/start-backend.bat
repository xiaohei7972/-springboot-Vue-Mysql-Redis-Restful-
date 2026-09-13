@echo off
set "PROJECT_ROOT=%~dp0.."
for %%I in ("%PROJECT_ROOT%") do set "PROJECT_ROOT=%%~fI"

call "%~dp0set-java21.bat"
if errorlevel 1 (
    pause
    exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
    echo ERROR: Maven was not found in PATH.
    echo Install Maven 3.9+ or add its bin directory to PATH.
    pause
    exit /b 1
)

if not exist "%PROJECT_ROOT%\pom.xml" (
    echo ERROR: Backend project was not found:
    echo   %PROJECT_ROOT%\pom.xml
    pause
    exit /b 1
)

title Student Management - Backend
pushd "%PROJECT_ROOT%"
echo Starting Spring Boot backend on http://localhost:8080
echo Press Ctrl+C to stop the backend.
echo.
mvn spring-boot:run
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%
