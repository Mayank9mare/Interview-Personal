@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET @@FAIL_FAST=
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_SAVE_ERRORLEVEL__=
@SET __MVNW_WRAPPER_PROPERTIES=%~dp0.mvn\wrapper\maven-wrapper.properties
@FOR /F "usebackq delims==" %%a IN ("%__MVNW_WRAPPER_PROPERTIES%") DO @(
  IF "%%a"=="distributionUrl" SET __MVNW_DISTRIBUTION_URL__=%%b
)
@SET JAVA_HOME_CANDIDATE=
@IF NOT "%JAVA_HOME%"=="" @SET JAVA_HOME_CANDIDATE=%JAVA_HOME%
@IF "%JAVA_HOME_CANDIDATE%"=="" @FOR /F "usebackq tokens=1* delims==" %%a IN (`where java 2^>nul`) DO @IF NOT "%%a"=="" @SET JAVA_HOME_CANDIDATE=%%~dpa..
@SET MVNW_VERBOSE=false
@IF NOT "%MVNW_VERBOSE%"=="true" @SET __MVNW_QUIET__=-q
@SET __MVNW_WRAPPER_JAR__=%~dp0.mvn\wrapper\maven-wrapper.jar
@IF NOT EXIST "%__MVNW_WRAPPER_JAR__%"  @(
  ECHO Cannot find .mvn\wrapper\maven-wrapper.jar
  EXIT /B 1
)
@"%JAVA_HOME_CANDIDATE%\bin\java.exe" -jar "%__MVNW_WRAPPER_JAR__%" %* 2>nul
@IF ERRORLEVEL 1 @"%JAVA_HOME_CANDIDATE%\bin\java.exe" %JVMCONFIG% -classpath "%__MVNW_WRAPPER_JAR__%" org.apache.maven.wrapper.MavenWrapperMain %*
