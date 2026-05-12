@echo off
SET "DIR=%~dp0"
SET "DIR=%DIR:~0,-1%"
"%JAVA_HOME%\bin\java.exe" -Dmaven.multiModuleProjectDirectory="%DIR%" -cp "%DIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
