@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements. See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership. The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License. You may obtain a copy of the License at
@REM
@REM https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied. See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_SAVE_ERRORLEVEL__=
@SET __MVNW_SAVE_CD__=%CD%
@SET __MVNW_WRAPPER_JAR__="%~dp0.mvn\wrapper\maven-wrapper.jar"
@SET __MVNW_PROJECT_BASEDIR__=%~dp0

@SETLOCAL

@SET DIRNAME=%~dp0
@IF "%DIRNAME%" == "" SET DIRNAME=.
@SET MAVEN_PROJECTBASEDIR=%DIRNAME%

@REM Try JAVA_HOME first
@IF NOT "%JAVA_HOME%" == "" (
  SET JAVA_CMD="%JAVA_HOME%\bin\java.exe"
) ELSE (
  SET JAVA_CMD=java
)

@SET MVNW_REPOURL=https://repo.maven.apache.org/maven2
@SET DISTRIBUTION_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip
@SET MAVEN_USER_HOME=%USERPROFILE%\.m2
@SET MAVEN_HOME_PARENT=%MAVEN_USER_HOME%\wrapper\dists
@SET MAVEN_HOME=%MAVEN_HOME_PARENT%\apache-maven-3.9.9
@SET MAVEN_EXE=%MAVEN_HOME%\bin\mvn.cmd

@IF EXIST "%MAVEN_EXE%" GOTO runMaven

@ECHO Downloading Apache Maven 3.9.9...
@MKDIR "%MAVEN_HOME_PARENT%" 2>NUL
@SET MAVEN_ZIP=%MAVEN_HOME_PARENT%\apache-maven-3.9.9-bin.zip
@powershell -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%MAVEN_ZIP%'"
@powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%MAVEN_HOME_PARENT%' -Force"
@DEL "%MAVEN_ZIP%"

:runMaven
@"%MAVEN_EXE%" %*
