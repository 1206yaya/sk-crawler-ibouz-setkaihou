@echo off
REM 'https://stackoverflow.com/questions/1645843/resolve-absolute-path-from-relative-path-and-or-file-name'
REM マルチプロジェクトのルートディレクトリの絶対パスをABS_PATHに得る

set REL_PATH=..\..\
set ABS_PATH=

rem // Save current directory and change to target directory
pushd %REL_PATH%

rem // Save value of CD variable current directory
set ABS_PATH=%CD%

rem // Restore original directory
popd

REM 実行するディレクトリはapplicationディレクトリでないと
REM System.getProperty user.dir　がapplicationを基準としないため、エラーになる

cd %~dp0
cd ../

REM cmd /k gradlew.bat :sk-crawler-ibouz-setkaihou-application:test -PisProduction=true --tests "sk.crawler.ibouz.setkaihou.SetKaihou" -i
cmd /k %ABS_PATH%/gradlew.bat :sk-crawler-ibouz-setkaihou-application:test -Denv=IS_PROD --tests "sk.crawler.ibouz.setkaihou.SetKaihou" -i


