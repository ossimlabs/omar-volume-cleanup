# OMAR VOLUME CLEANUP

## Purpose

The OMAR Volume Cleanup application serves as a file management services to prevent the disk from becoming full.

## Installation in Openshift

**Assumption:** The omar-volume-cleanup docker image is pushed into the OpenShift server's internal docker registry and available to the project.

### Persistent Volumes

OMAR Volume Cleanup requires shared access to OSSIM imagery data. This data is assumed to be accessible from the local filesystem of the pod. Therefore, a volume mount must be mapped into the container. A PersistentVolumeClaim should be mounted to a configured location (see environment variables) in the service, but is typically */data*

### Environment variables

|Variable|Value|
|------|------|
|CLEANUP_DRYRUN|If true, don't delete any rasters and only log the images (*e.g. true, false*)|
|CLEANUP_VOLUME|The root path for raster images (*e.g. /data*|
|CLEANUP_DELAY|The delay between size checks in HOCON duration format (*e.g. 10M, 5M*)|
|CLEANUP_PERCENT|The percentage size limit for the volume (*e.g. 0.95*)|
|CLEANUP_RASTERENDPOINT|The stage endpoint excluding "/dataManager/removeRaster" (*e.g. https://omar-dev/raster/*)|
|DATABASE_URL|The full JDBC url to the Omar database (*e.g. jdbc://test/db/url:1234*)|
|DATABASE_USERNAME|The username for the Omar database|
|DATABASE_PASSWORD|The password for the Omar database|