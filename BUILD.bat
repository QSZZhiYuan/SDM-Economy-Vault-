@echo off
echo ========================================
echo SDM-Economy Build Script
echo ========================================
echo.

echo Step 1/4: Cleaning old build files...
if exist build rd /S /Q build 2>nul
if exist .gradle rd /S /Q .gradle 2>nul
if exist common\build rd /S /Q common\build 2>nul
if exist forge\build rd /S /Q forge\build 2>nul

echo.
echo Step 2/4: Cleaning corrupted cache...
set CACHE_PATH=%USERPROFILE%\.gradle\caches
if exist "%CACHE_PATH%\modules-2\files-2.1\dev.architectury" (
    rd /S /Q "%CACHE_PATH%\modules-2\files-2.1\dev.architectury" 2>nul
)
if exist "%CACHE_PATH%\fabric-loom" (
    rd /S /Q "%CACHE_PATH%\fabric-loom" 2>nul
)

echo.
echo Step 3/4: Building (Forge only, estimated 10-15 minutes)...
echo Please wait...
gradlew.bat :forge:build --no-daemon --refresh-dependencies

echo.
echo Step 4/4: Checking build result...
if exist forge\build\libs\SDMEconomy-1.20.1-forge-2.3.0.jar (
    echo.
    echo ========================================
    echo BUILD SUCCESS!
    echo ========================================
    echo.
    echo Generated file: forge\build\libs\SDMEconomy-1.20.1-forge-2.3.0.jar
    echo.
    echo Opening folder...
    start "" explorer forge\build\libs
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo Please send me the error message above.
    echo.
)

echo.
pause
