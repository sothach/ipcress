# Deployment commands

## Build Docker image
`% sbt docker:publishLocal`

## Test docker image
`% docker run -p 9000:9000 ipcress:1.0`

## Deploy to k8s (DevSpace)
```bash
% npm install -g devspace
% brew install kubectl
% devspace connect cluster
% devspace init --dockerfile target/docker/stage/Dockerfile
% devspace create space ipcress
% sbt docker:publish
% devspace deploy --skip-build
% devspace open --var="play.http.secret.key=*secret*"
```

## Add application secret to cloud environment
```bash
APPLICATION_SECRET=`sbt playGenerateSecret`
```
E.g., locally:
```bash
% docker run -p 9000:9000 -e play.http.secret.key=$APPLICATION_SECRET dscr.io/sothach/ipcress:latest
```