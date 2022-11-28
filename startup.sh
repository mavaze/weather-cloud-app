#!/bin/bash

# HEADER='\033[95m'
# OKBLUE='\033[94m'
# OKCYAN='\033[96m'
# WARNING='\033[93m'
# UNDERLINE='\033[4m'

HIGHLIGHT='\033[92m'
FAIL='\033[91m'
NOCOLOR='\033[0m\n'
BOLD='\033[1m'

printf "${BOLD}${HIGHLIGHT}*** [STEP 1] Building application executable binaries${NOCOLOR}"
# mvn clean install >> /dev/null 2>&1
mvn clean install -DskipTests >> /dev/null 2>&1
rc=$?
if [ $rc -ne 0 ] ; then
  printf "${BOLD}${FAIL}Could not perform mvn clean install, exit code [$rc]${NOCOLOR}"; exit $rc
fi

echo
printf "${BOLD}${HIGHLIGHT}*** [STEP 2] Building images from Dockerfile listed in docker-compose.yaml${NOCOLOR}"
docker-compose build
rc=$?
if [ $rc -ne 0 ] ; then
  printf "${BOLD}${FAIL}Failed to build docker images, exit code [$rc]${NOCOLOR}"; exit $rc
fi

echo
printf "${BOLD}${HIGHLIGHT}*** [STEP 3] Checking name resolution${NOCOLOR}"
getent hosts weather-cloud-api weather-cloud-gateway weather-cloud-security
rc=$?
if [ $rc -ne 0 ] ; then
  printf "${BOLD}${FAIL}Either of hosts weather-cloud-api weather-cloud-gateway weather-cloud-security not resolved.${NOCOLOR}"
  printf "${BOLD}${FAIL}Make sure you have them added in /etc/hosts either before running containers.${NOCOLOR}"
  exit $rc
fi

echo
printf "${BOLD}${HIGHLIGHT}*** [STEP 4] Running containers in the background${NOCOLOR}"
# if [ "$(docker-compose ps -q)" ]; then
#     if [ "$(docker-compose ps -q --filter status=exited)" ]; then
#         printf "${RED}Docker containers already running. Bringing them down before starting them again.${NOCOLOR}"
#         docker-compose down
#     fi
# fi
docker-compose up -d 

echo
printf "${BOLD}${HIGHLIGHT}Run \`docker-compose logs -f\` for logs or \`docker-compose down\` to stop all associated containers${NOCOLOR}"
echo
