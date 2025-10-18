@echo off
echo ========================================
echo Ultimate Cache Fix - Complete Cleanup
echo ========================================
echo.
echo WARNING: This will delete your ENTIRE Gradle cache!
echo This is safe but will require re-downloading all dependencies.
echo.
echo Press Ctrl+C to cancel, or
pause

echo.
echo Step 1: Stopping all Gradle processes...
taskkill /F /IM java.exe 2>nul
taskkill /F /IM javaw.exe 2>nul
timeout /t 3 >nul

echo.
echo Step 2: Deleting ENTIRE Gradle cache...
set GRADLE_HOME=%USERPROFILE%\.gradle
if exist "%GRADLE_HOME%\caches" (
    echo Deleting %GRADLE_HOME%\caches ...
    rd /S /Q "%GRADLE_HOME%\caches" 2>nul
    echo Done.
)
if exist "%GRADLE_HOME%\loom-cache" (
    echo Deleting %GRADLE_HOME%\loom-cache ...
    rd /S /Q "%GRADLE_HOME%\loom-cache" 2>nul
    echo Done.
)
if exist "%GRADLE_HOME%\wrapper" (
    echo Deleting %GRADLE_HOME%\wrapper ...
    rd /S /Q "%GRADLE_HOME%\wrapper" 2>nul
    echo Done.
)

echo.
echo Step 3: Cleaning project files...
if exist build rd /S /Q build 2>nul
if exist .gradle rd /S /Q .gradle 2>nul
if exist common\build rd /S /Q common\build 2>nul
if exist forge\build rd /S /Q forge\build 2>nul
if exist common\.gradle rd /S /Q common\.gradle 2>nul
if exist forge\.gradle rd /S /Q forge\.gradle 2>nul

echo.
echo Step 4: Starting fresh build...
echo This will take 20-30 minutes as everything needs to be re-downloaded.
echo.
gradlew.bat :forge:build --no-daemon 2>&1 | tee build.log

echo.
if exist forge\build\libs\SDMEconomy-1.20.1-forge-2.3.0.jar (
    echo ========================================
    echo BUILD SUCCESS!
    echo ========================================
    echo.
    dir forge\build\libs\*.jar
    start "" explorer forge\build\libs
) else (
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo Check build.log for details
    echo.
    echo Common solutions:
    echo 1. Check your internet connection
    echo 2. Disable antivirus/firewall temporarily
    echo 3. Try running as Administrator
    echo 4. Make sure you have JDK 17 installed
)

pause
