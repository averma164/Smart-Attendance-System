@echo off
:: ============================================================
::  Smart Attendance System — Compile & Run Script
::  Place the MySQL Connector/J JAR in the lib\ folder first.
::  Then double-click this file or run from a terminal.
:: ============================================================

setlocal

set SRC_DIR=src
set OUT_DIR=out
set LIB_DIR=lib

:: Find the MySQL connector JAR (any version)
set MYSQL_JAR=
for %%f in (%LIB_DIR%\mysql-connector-*.jar) do set MYSQL_JAR=%%f

if "%MYSQL_JAR%"=="" (
    echo.
    echo  ERROR: No MySQL Connector JAR found in the lib\ folder.
    echo  Download it from https://dev.mysql.com/downloads/connector/j/
    echo  and place the JAR file inside:  %~dp0lib\
    echo.
    pause
    exit /b 1
)

echo Using JDBC driver: %MYSQL_JAR%
echo.

:: Create output directory
if not exist %OUT_DIR% mkdir %OUT_DIR%

:: Compile all Java source files
echo Compiling source files...
javac -cp "%MYSQL_JAR%" -d %OUT_DIR% %SRC_DIR%\*.java
if %ERRORLEVEL% neq 0 (
    echo.
    echo  Compilation FAILED. Check error messages above.
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo Starting Smart Attendance System...
echo.

:: Run the application
java -cp "%OUT_DIR%;%MYSQL_JAR%" Main

endlocal
pause
