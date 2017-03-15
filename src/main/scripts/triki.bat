@echo off
set contentDir=%1
set port=%2

if "%contentDir%" == "" (
   echo "Usage: triki.sh <content dir> <port>"
   exit /b -1
)

if "%port%" == "" (
   echo "Usage: triki.sh <content dir> <port>"
   exit /b -1
)

echo "Starting triki..."
java -Dcontent_dir=%contentDir% -Dport=%port% -jar ..\lib\triki.jar
