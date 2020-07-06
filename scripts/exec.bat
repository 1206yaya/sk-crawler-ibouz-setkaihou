@echo off
cd %~dp0
cd ../


cmd /k gradlew.bat :sk-crawler-ibouz-setkaihou-application:test -PisProduction=true --tests "sk.crawler.ibouz.setkaihou.SetKaihou" -i


