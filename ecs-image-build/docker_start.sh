#!/bin/bash
# 
# Start script for officer-filing-api

PORT=8080

exec java -jar -Dserver.port="${PORT}" "officer-filing-api.jar"
 