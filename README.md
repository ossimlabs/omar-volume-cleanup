[![Build Status](https://jenkins.ossim.io/buildStatus/icon?job=omar-volume-cleanup/master)](https://jenkins.ossim.io/job/omar-volume-cleanup/master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ossimlabs_omar-volume-cleanup&metric=alert_status)](https://sonarcloud.io/dashboard?id=ossimlabs_omar-volume-cleanup)

An application to monitor and manage a volume of O2 raster images.

The application cleans old rasters when used disk space goes over a given threshold. Rasters are deleted using the omar-stager HTTP API.

# Quickstart

## Development
To build and run tests use:
 ```
 ./gradlew build
 ```

## Docker
Generate a a Docker image to with:
```
./gradlew jibDockerBuild --image=omar-volume-cleanup
```

The following are the environment variables with their default values.
Fill in the missing environment variables for your environment and bind 
your raster volume to the `/data` mount in the container.
```$xslt
docker run -it --rm \
  --env CLEANUP_DRYRUN=true \
  --env CLEANUP_VOLUME=/data \
  --env CLEANUP_DELAY=10m \
  --env CLEANUP_PERCENT=0.95 \
  --env CLEANUP_RASTERENDPOINT= \
  --env DATABASE_URL= \
  --env DATABASE_USERNAME= \
  --env DATABASE_PASSWORD= \
  -v <YOUR_DIRECTORY_HERE>:/data \
  omar-volume-cleanup
```
