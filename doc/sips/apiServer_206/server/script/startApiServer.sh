#! /usr/bin/ksh
#-------------------------------------------------------------
#   Script de Lancement API Server seul
#-------------------------------------------------------------

SERVER_ROOT="chemin du repertoire server"

#-------------------------------------------------------------

ARCHIVES="$SERVER_ROOT/lib/apiServer.jar:"

POLICY="$SERVER_ROOT/rmi/client.policy"
CONFIG_FILE="$SERVER_ROOT/config/config.xml"

CLASS=com.atosorigin.services.cad.apiserver.BootThread

java -cp $ARCHIVES -Djava.security.policy=$POLICY -Dconfigfile=$CONFIG_FILE $CLASS alone
