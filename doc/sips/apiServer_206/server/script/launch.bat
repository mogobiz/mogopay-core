ECHO OFF
REM -------------------------------------------------------------
REM   Script de Lancement API Server
REM -------------------------------------------------------------

SET SERVER_ROOT=c:\server

REM -------------------------------------------------------------

SET ARCHIVES=%SERVER_ROOT%\lib\apiServer.jar;

SET POLICY=%SERVER_ROOT%\rmi\client.policy
SET CONFIG_FILE=%SERVER_ROOT%\config\config.xml

SET CLASS=com.atosorigin.services.cad.apiserver.watchdog.ProcessWatchdog

ECHO ON

java -cp "%ARCHIVES%" -Djava.security.policy="%POLICY%" -Dconfigfile="%CONFIG_FILE%" %CLASS%
 
