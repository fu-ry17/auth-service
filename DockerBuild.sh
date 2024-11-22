#!/bin/sh
REGISTRY=10.0.2.2
if [ -n "$1" ]; then
  REGISTRY="$1"
fi

REGISTRY_LOCATION=/agencify
if [ -n "$2" ]; then
  REGISTRY_LOCATION="$2"
fi

REGISTRY_ACTIVE_PROFILE=kube
if [ -n "$3" ]; then
  REGISTRY_ACTIVE_PROFILE="$3"
fi

INGRESS_ROUTE=kube
if [ -n "$4" ]; then
  INGRESS_ROUTE="$4"
fi

COMMIT_TAG=unknowncommit
if [ -n "$5" ]; then
  COMMIT_TAG="$5"
fi



# Get Service Name from Gradle Properties
TEMP_SERVICE_NAME="$(egrep  '(^APP_NAME)' gradle.properties)"
SERVICE_NAME="$(echo $TEMP_SERVICE_NAME |  cut -d = -f 2)"

# Get Docker Tag
TEMP_TAG="$(egrep  '(^appVersionName)' gradle.properties)"
SERVICE_TAG="$(echo $TEMP_TAG |  cut -d = -f 2)"

# Get Git Commit Log
#COMMIT_TAG="$(git log -n 1 --pretty=format:'%h')"

# Create Docker Image Name
DOCKER_IMAGE="$REGISTRY/$REGISTRY_LOCATION/$SERVICE_NAME:$SERVICE_TAG-$COMMIT_TAG"

echo "Service Name $SERVICE_NAME and Registry is $REGISTRY Service_TAG $SERVICE_TAG Docker Image is $DOCKER_IMAGE Commit Tag $COMMIT_TAG"
echo "Docker Image is $DOCKER_IMAGE"
echo "Preparing Dockerfile"
#Active Profile
sed -i.bak 's#TargetActiveProfile=kube#TargetActiveProfile='$REGISTRY_ACTIVE_PROFILE'#' Dockerfile
#Registry Location. 
sed -i.bak 's#TargetRegistryLocation=agencify#TargetRegistryLocation='$REGISTRY_LOCATION'#' Dockerfile
#Spring Active Profile. 
sed -i.bak 's#spring.profiles.active=kube#spring.profiles.active='$REGISTRY_ACTIVE_PROFILE'#' Dockerfile
echo "Building Docker Image"
docker build -t $DOCKER_IMAGE .
echo "Push Docker Image to Registry"
docker push  $DOCKER_IMAGE


