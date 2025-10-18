@echo off
setlocal enabledelayedexpansion

echo ========================================
echo SDM-Economy Cache Fix and Build Script V2
echo ========================================
echo.

echo This script will:
echo 1. Clean all caches
echo 2. Build the mod from scratch
echo 3. Keep the window open even if errors occur
echo.
echo Press any key to continue...
pause >nul

echo.
echo Checking if gradlew.bat exists...
if not exist gradlew.bat (
    echo ERROR: gradlew.bat not found!
    echo Please make sure you are running this script in the SDM-Economy-1.20.1 folder.
    echo Current directory: %CD%
    goto ERROR_EXIT
)
echo OK: gradlew.bat found

echo.
echo ========================================
echo Step 1/6: Stopping Gradle Daemon
echo ========================================
call gradlew.bat --stop
if errorlevel 1 (
    echo Warning: Failed to stop daemon, continuing anyway...
)
timeout /t 2 >nul

echo.
echo ========================================
echo Step 2/6: Cleaning Project Files
echo ========================================
if exist build (
    echo Deleting build folder...
    rd /S /Q build 2>nul
)
if exist .gradle (
    echo Deleting .gradle folder...
    rd /S /Q .gradle 2>nul
)
if exist common\build (
    echo Deleting common\build folder...
    rd /S /Q common\build 2>nul
)
if exist forge\build (
    echo Deleting forge\build folder...
    rd /S /Q forge\build 2>nul
)
echo Project files cleaned.

echo.
echo ========================================
echo Step 3/6: Cleaning Global Gradle Cache
echo ========================================
set GRADLE_HOME=%USERPROFILE%\.gradle

if exist "%GRADLE_HOME%\caches\fabric-loom" (
    echo Cleaning Loom cache...
    rd /S /Q "%GRADLE_HOME%\caches\fabric-loom" 2>nul
    echo Done.
)

if exist "%GRADLE_HOME%\caches\modules-2\files-2.1\dev.architectury" (
    echo Cleaning Architectury cache...
    rd /S /Q "%GRADLE_HOME%\caches\modules-2\files-2.1\dev.architectury" 2>nul
    echo Done.
)

if exist "%GRADLE_HOME%\loom-cache" (
    echo Cleaning Loom global storage...
    rd /S /Q "%GRADLE_HOME%\loom-cache" 2>nul
    echo Done.
)

echo All caches cleaned.

echo.
echo ========================================
echo Step 4/6: Preparing Build
echo ========================================
call gradlew.bat --stop
timeout /t 1 >nul

echo.
echo ========================================
echo Step 5/6: Building (15-20 minutes)
echo ========================================
echo Starting build... Please wait...
echo.
echo This will output a LOT of text. Don't worry, it's normal.
echo The build log will be saved to build.log
echo.

call gradlew.bat :forge:build --no-daemon --refresh-dependencies --no-build-cache > build.log 2>&1

if errorlevel 1 (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo Last 50 lines of build.log:
    echo.
    powershell -Command "Get-Content build.log -Tail 50"
    echo.
    echo Full log saved to: build.log
    echo.
    goto ERROR_EXIT
)

echo.
echo ========================================
echo Step 6/6: Checking Result
echo ========================================
if exist forge\build\libs\SDMEconomy-1.20.1-forge-2.3.0.jar (
    echo.
    echo ========================================
    echo BUILD SUCCESS!
    echo ========================================
    echo.
    echo Generated file: forge\build\libs\SDMEconomy-1.20.1-forge-2.3.0.jar
    echo.
    dir forge\build\libs\*.jar
    echo.
    echo Opening folder...
    start "" explorer forge\build\libs
    echo.
    echo Press any key to close...
    pause >nul
    exit /b 0
) else (
    echo.
    echo ERROR: JAR file not found!
    echo Expected: forge\build\libs\SDMEconomy-1.20.1-forge-2.3.0.jar
    echo.
    goto ERROR_EXIT
)

:ERROR_EXIT
echo.
echo ========================================
echo SCRIPT STOPPED DUE TO ERROR
echo ========================================
echo.
echo Please:
echo 1. Check build.log in this folder for details
echo 2. Send me the last 50 lines shown above
echo 3. Check that you have JDK 17 installed
echo.
echo Press any key to exit...
pause >nul
exit /b 1
