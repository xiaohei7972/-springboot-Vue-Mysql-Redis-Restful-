@echo off
rem Configure JDK 21 for the current command window and future user sessions.

if not defined JDK21 (
    echo ERROR: JDK21 is not defined.
    echo Define it first, for example:
    echo   setx JDK21 "D:\Environment\Java\jdk-21.0.10"
    exit /b 1
)

if not exist "%JDK21%\bin\java.exe" (
    echo ERROR: JDK21 does not point to a valid JDK:
    echo   %JDK21%
    exit /b 1
)

set "JAVA_HOME=%JDK21%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

setx JAVA_HOME "%JDK21%" >nul
if errorlevel 1 (
    echo WARNING: Could not persist JAVA_HOME. The current window is configured.
) else (
    echo JAVA_HOME was saved for future command windows.
)

echo.
java -version
echo.
echo Current JAVA_HOME=%JAVA_HOME%
exit /b 0
