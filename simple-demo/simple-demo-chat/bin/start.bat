@echo off
echo Starting application. Please wait...

cd ..

echo %CLASSPATH%

set CLASSPATH=etc/.;lib/*;~1%;.

echo %CLASSPATH%

rem java "%JAVA_OPTS%" -classpath %CLASSPATH% org.simpleframework.demo.ApplicationLauncher etc/spring.xml etc/common.properties etc/local.properties 
java  "%JAVA_OPTS%"  -classpath %CLASSPATH% org.simpleframework.demo.ApplicationLauncher etc/spring.xml etc/common.properties etc/local.properties 

