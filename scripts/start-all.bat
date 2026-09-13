@echo off
call "%~dp0set-java21.bat"
if errorlevel 1 (
    pause
    exit /b 1
)

echo Opening backend and frontend command windows...
start "Student Management - Backend" cmd /k call "%~dp0start-backend.bat"
start "Student Management - Frontend" cmd /k call "%~dp0start-frontend.bat"
echo.
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5173
echo.
pause
