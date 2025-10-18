@echo off
echo ========================================
echo SDM-Economy Cache Fix and Build Script
echo ========================================
echo.

echo IMPORTANT: This will clean ALL Gradle caches for this project.
echo This is necessary to fix the Minecraft remap error.
echo.
pause

echo.
echo ========================================
echo Step 1/6: Stopping Gradle Daemon
echo ========================================
gradlew.bat --stop
timeout /t 2 >nul

echo.
echo ========================================
echo Step 2/6: Cleaning Project Files
echo ========================================
if exist build rd /S /Q build 2>nul
if exist .gradle rd /S /Q .gradle 2>nul
if exist common\build rd /S /Q common\build 2>nul
if exist forge\build rd /S /Q forge\build 2>nul
if exist common\.gradle rd /S /Q common\.gradle 2>nul
if exist forge\.gradle rd /S /Q forge\.gradle 2>nul
echo Project files cleaned.

echo.
echo ========================================
echo Step 3/6: Cleaning Global Gradle Cache
echo ========================================
set GRADLE_HOME=%USERPROFILE%\.gradle

echo Cleaning Loom cache...
if exist "%GRADLE_HOME%\caches\fabric-loom" (
    rd /S /Q "%GRADLE_HOME%\caches\fabric-loom" 2>nul
    echo - Loom cache deleted
)

echo Cleaning Architectury cache...
if exist "%GRADLE_HOME%\caches\modules-2\files-2.1\dev.architectury" (
    rd /S /Q "%GRADLE_HOME%\caches\modules-2\files-2.1\dev.architectury" 2>nul
    echo - Architectury cache deleted
)

echo Cleaning MinecraftForge cache...
if exist "%GRADLE_HOME%\caches\forge_gradle" (
    rd /S /Q "%GRADLE_HOME%\caches\forge_gradle" 2>nul
    echo - Forge Gradle cache deleted
)

echo Cleaning Minecraft cache...
if exist "%GRADLE_HOME%\caches\minecraft" (
    rd /S /Q "%GRADLE_HOME%\caches\minecraft" 2>nul
    echo - Minecraft cache deleted
)

echo.
echo ========================================
echo Step 4/6: Cleaning Loom Global Storage
echo ========================================
if exist "%GRADLE_HOME%\loom-cache" (
    rd /S /Q "%GRADLE_HOME%\loom-cache" 2>nul
    echo - Loom global cache deleted
)

echo.
echo ========================================
echo Step 5/6: Building (This may take 15-20 minutes)
echo ========================================
echo Please be patient, this will download and setup everything fresh...
echo.
gradlew.bat --stop
gradlew.bat :forge:build --no-daemon --refresh-dependencies --no-build-cache

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
    echo Generated: forge\build\libs\SDMEconomy-1.20.1-forge-2.3.0.jar
    echo.
    start "" explorer forge\build\libs
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo If you still get errors, try these:
    echo.
    echo 1. Make sure you have JDK 17 installed
    echo 2. Check your internet connection
    echo 3. Try running this script again
    echo 4. Send me the full error log
    echo.
)

echo.
pause
