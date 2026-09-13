@echo off
set "PROJECT_ROOT=%~dp0.."
for %%I in ("%PROJECT_ROOT%") do set "PROJECT_ROOT=%%~fI"

where npm >nul 2>nul
if errorlevel 1 (
    echo ERROR: npm was not found in PATH.
    echo Install Node.js 20+ and reopen this command window.
    pause
    exit /b 1
)

if not exist "%PROJECT_ROOT%\frontend\package.json" (
    echo ERROR: Frontend project was not found:
    echo   %PROJECT_ROOT%\frontend\package.json
    pause
    exit /b 1
)

if not exist "%PROJECT_ROOT%\frontend\node_modules" (
    echo ERROR: frontend\node_modules was not found.
    echo Run the following command in the frontend directory first:
    echo   npm install
    pause
    exit /b 1
)

title Student Management - Frontend
pushd "%PROJECT_ROOT%\frontend"
echo Starting Vue frontend on http://localhost:5173
echo Press Ctrl+C to stop the frontend.
echo.
npm run dev
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%
