@echo off
setlocal

echo ==============================================
echo     Secure Banking System - Portable Build
echo ==============================================

:: Portable Java Detection: Check local jre first, then system PATH
if exist "jre\bin\java.exe" (
    set "JAVA=jre\bin\java.exe"
    set "JAVAC=jre\bin\javac.exe"
) else (
    :: Use system Java from PATH
    where java >nul 2>nul
    if %errorlevel% neq 0 (
        echo [ERROR] Java not found in PATH.
        echo Please install Java 17 or higher.
        pause
        exit /b
    )
    set "JAVA=java"
    set "JAVAC=javac"
)

echo [1/3] Using Java: %JAVA%

:: 2. Compile everything
echo [2/3] Compiling Secure Banking System (JavaFX)...
if not exist bin mkdir bin

set "CLASSPATH=bin;lib\*"
set "FX_MODULES=--module-path lib\javafx-sdk\lib --add-modules javafx.controls,javafx.fxml,javafx.graphics"

:: Generate sources list
dir /s /B src\*.java > sources.txt

:: Compile
%JAVAC% %FX_MODULES% -d bin -cp "%CLASSPATH%;src" @sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    del sources.txt
    pause
    exit /b
)
del sources.txt

:: Copy Resources
echo [Copying Resources...]
if not exist bin\banking\resources mkdir bin\banking\resources
xcopy /s /y /i src\banking\resources\* bin\banking\resources\ >nul

:: 3. Run the App
echo [3/3] Launching JavaFX System...
%JAVA% -Dprism.order=sw %FX_MODULES% -cp "%CLASSPATH%;bin" banking.application.MainAppFX

pause
