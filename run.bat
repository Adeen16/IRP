@echo off
setlocal

echo ==============================================
echo     Secure Banking System - Portable Build
echo ==============================================

:: Hardcoded path to the known working Java on this machine
set "JAVA_DIR=C:\Program Files\JetBrains\PyCharm Community Edition 2024.3.4\jbr"
if not exist "%JAVA_DIR%" (
    :: Fallback to Android Studio if PyCharm JBR is missing
    set "JAVA_DIR=C:\Program Files\Android\Android Studio\jbr"
)

set "JAVA_EXEC=%JAVA_DIR%\bin\java.exe"
set "JAVAC_EXEC=%JAVA_DIR%\bin\javac.exe"

echo [1/3] Using Java: "%JAVA_EXEC%"

:: 2. Compile everything
echo [2/3] Compiling Secure Banking System (JavaFX)...
if not exist bin mkdir bin

set "CLASSPATH=bin;lib\*"
set "FX_MODULES=--module-path lib\javafx-sdk\lib --add-modules javafx.controls,javafx.fxml,javafx.graphics"

:: Generate sources list
dir /s /B src\*.java > sources.txt

:: Compile
"%JAVAC_EXEC%" %FX_MODULES% -d bin -cp "%CLASSPATH%;src" @sources.txt
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
"%JAVA_EXEC%" -Dprism.order=sw %FX_MODULES% -cp "%CLASSPATH%;bin" banking.application.MainAppFX

pause
