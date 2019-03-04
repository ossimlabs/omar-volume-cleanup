[![Build Status](https://jenkins.ossim.io/buildStatus/icon?job=omar-volume-cleanup/master)](https://jenkins.ossim.io/job/omar-volume-cleanup/master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ossimlabs_omar-volume-cleanup&metric=alert_status)](https://sonarcloud.io/dashboard?id=ossimlabs_omar-volume-cleanup)

# Quickstart

## Docker
The following are the environemtn variables with their default values.
Fill in the missing environment variables for your environment and bind 
your raster volume to the `/raster` mount in the container.
```$xslt
docker run -it --rm \
  --env CLEANUP_DRYRUN=true \
  --env CLEANUP_VOLUME=/raster \
  --env CLEANUP_DELAY=10m \
  --env CLEANUP_PERCENT=0.95 \
  --env CLEANUP_RASTERENDPOINT= \
  --env DATABASE_URL= \
  --env DATABASE_USERNAME= \
  --env DATABASE_PASSWORD= \
  -v <YOUR_DIRECTORY_HERE>:/raster \
  omar-volume-cleanup
```