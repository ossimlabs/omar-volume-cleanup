# OMAR VOLUME CLEANUP

## Purpose

The OMAR Volume Cleanup application serves as a filemanagement services to prevent the disk from becoming full.

## Installation in Openshift

**Assumption:** The omar-volume-cleanup docker image is pushed into the OpenShift server's internal docker registry and available to the project.

### Persistent Volumes

OMAR Volume Cleanup requires shared access to OSSIM imagery data. This data is assumed to be accessible from the local filesystem of the pod. Therefore, a volume mount must be mapped into the container. A PersistentVolumeClaim should be mounted to a configured location (see environment variables) in the service, but is typically */data*

### Environment variables

|Variable|Value|
|------|------|
|SPRING_PROFILES_ACTIVE|Comma separated profile tags (*e.g. production, dev*)|
|SPRING_CLOUD_CONFIG_LABEL|The Git branch from which to pull config files (*e.g. master*)|
|OSSIM_PREFS_FILE|The location of the preferences file (*e.g. /usr/share/ossim/ossim-site-preferences*)|
|OSSIM_INSTALL_PREFIX|The directory in which OSSIM is installed (*e.g. /usr*)|
|OSSIM_DATA|The location of OSSIM imagery data such as elevation (*e.g. /data*)|
|BUCKETS|The S3 to mount for direct image access (*e.g. my-bucket*)|
|JAVA_OPTS|JVM Runtime options required for the service (*e.g. -server -Xms256m -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1  -XX:+CMSClassUnloadingEnabled -XX:+UseGCOverheadLimit -Djava.awt.headless=true -XshowSettings:vm -Djava.security.egd=file:/dev/./urandom*)