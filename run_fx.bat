@echo off
setlocal enabledelayedexpansion

echo ==============================================
echo     Secure Banking System - JavaFX Build
echo ==============================================

:: 1. Try to find Java Path
set JAVA_EXEC=java
where %JAVA_EXEC% >nul 2>nul
if %errorlevel% neq 0 (
    set JAVA_HOME_PATH=
    for /d %%i in ("C:\Program Files\Java\jdk*") do set JAVA_HOME_PATH=%%i
    for /d %%i in ("C:\Program Files\Java\jre*") do if not defined JAVA_HOME_PATH set JAVA_HOME_PATH=%%i
    
    if not defined JAVA_HOME_PATH (
        echo [ERROR] Java JDK not found.
        echo Please ensure Java is installed.
        pause
        exit /b
    )
    set JAVA_EXEC="!JAVA_HOME_PATH!\bin\java.exe"
    set JAVAC_EXEC="!JAVA_HOME_PATH!\bin\javac.exe"
) else (
    set JAVA_EXEC=java
    set JAVAC_EXEC=javac
)

echo [1/3] Using Java: !JAVA_EXEC!

:: 2. Compile everything
echo [2/3] Compiling Secure Banking System (JavaFX)...
if not exist bin mkdir bin

:: Find all jars in lib including the standard ones
set CLASSPATH=bin;lib\*

:: JavaFX Modules
set FX_MODULES=--module-path lib\javafx-sdk\lib --add-modules javafx.controls,javafx.fxml,javafx.graphics

:: Find all java files
dir /s /B src\*.java > sources.txt

:: Compile
!JAVAC_EXEC! %FX_MODULES% -d bin -cp "%CLASSPATH%;src" @sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed. Check syntax.
    del sources.txt
    pause
    exit /b
)
del sources.txt

:: Copy CSS and FXML to bin (since javac doesn't do it)
echo [Copying Resources...]
xcopy /s /y /i src\banking\resources\* bin\banking\resources\ >nul

:: 3. Run the App
echo [3/3] Launching JavaFX System...
!JAVA_EXEC! %FX_MODULES% -cp "%CLASSPATH%;bin" banking.application.MainAppFX

pause
