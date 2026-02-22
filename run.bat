@echo off
setlocal enabledelayedexpansion

echo ==============================================
echo     Secure Banking System - Final Build
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

echo [1/3] Using Java: %JAVA_EXEC%

:: 2. Compile everything
echo [2/3] Compiling Secure Banking System...
if not exist bin mkdir bin

:: Find all jars in lib
set CLASSPATH=bin;lib\*

:: Use a robust compile command
dir /s /B src\*.java > sources.txt
%JAVAC_EXEC% -d bin -cp "%CLASSPATH%;src" @sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    del sources.txt
    pause
    exit /b
)
del sources.txt

:: 3. Run the App
echo [3/3] Launching System...
echo Note: If the database is missing, please run schema.sql first.
%JAVA_EXEC% -cp "%CLASSPATH%" banking.MainApp

pause
