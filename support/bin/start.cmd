@echo off 
if exist %JAVA_HOME% (goto runapp) else (goto notfound)

:runapp
set APP_HOME=%~dp0..
:: -cp -classpath 缩写
java -cp "%APP_HOME%\dbproxy-services_2.10-0.1.jar;%APP_HOME%\conf;%APP_HOME%\lib\*" com.hikvision.dbproxy.services.Boot
goto end

:notfound
echo not found jdk should install it. 
goto end

:end
cd "%CURRENT_DIR%"