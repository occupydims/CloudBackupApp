@echo off
set DIR=%~dp0
set JAVA_EXEC=java
if not "%JAVA_HOME%"=="" set JAVA_EXEC=%JAVA_HOME%\bin\java
"%JAVA_EXEC%" -Dorg.gradle.appname=gradlew -classpath "%DIR%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
