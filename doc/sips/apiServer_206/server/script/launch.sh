#! /usr/bin/ksh
#-------------------------------------------------------------
#   Script de Lancement API Server
#-------------------------------------------------------------

SERVER_ROOT="chemin du repertoire server"

#-------------------------------------------------------------

ARCHIVES="$SERVER_ROOT/lib/apiServer.jar:"

POLICY="$SERVER_ROOT/rmi/client.policy"
CONFIG_FILE="$SERVER_ROOT/config/config.xml"

CLASS=com.atosorigin.services.cad.apiserver.watchdog.ProcessWatchdog

java -cp $ARCHIVES -Djava.security.policy=$POLICY -Dconfigfile=$CONFIG_FILE $CLASS
