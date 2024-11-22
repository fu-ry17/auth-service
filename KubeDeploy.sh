#!/bin/sh
REGISTRY=10.0.2.2
if [ -n "$1" ]; then
  REGISTRY="$1"
fi

REGISTRY_LOCATION=/agencify
if [ -n "$2" ]; then
  REGISTRY_LOCATION="$2"
fi

NAMESPACE=defaultxxxx
if [ -n "$3" ]; then
  NAMESPACE="$3"
fi

INGRESS_ROUTE=defaultxxxx
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
# COMMIT_TAG="$(git log -n 1 --pretty=format:'%h')"

# Create Docker Image Name
DOCKER_IMAGE="$REGISTRY/$REGISTRY_LOCATION/$SERVICE_NAME:$SERVICE_TAG-$COMMIT_TAG"

echo "Service Name $SERVICE_NAME and Registry is $REGISTRY Service_TAG $SERVICE_TAG Docker Image is $DOCKER_IMAGE Commit Tag $COMMIT_TAG"
echo "Docker Image is $DOCKER_IMAGE"
echo "NAMESPACE is $NAMESPACE"
echo "Preparing Deployfile"
#Active Profile
sed -i.bak 's#REGISTRY_LOCATION#'$DOCKER_IMAGE'#' k8stemplates/app.yaml
sed -i.bak 's#INGRESS_ROUTE#'$INGRESS_ROUTE'#' k8stemplates/app.yaml

#echo "Building Docker Image"
#kubectl delete -f k8stemplates/app.yaml -n $NAMESPACE
kubectl apply -f k8stemplates/app.yaml -n $NAMESPACE



