String buildNodeDefault = "omar-build"

//noinspection GroovyAssignabilityCheck
properties([
        parameters([
                string(name: 'BUILD_NODE', defaultValue: buildNodeDefault, description: 'The build node to run on'),
                booleanParam(name: 'CLEAN_WORKSPACE', defaultValue: true, description: 'Clean the workspace at the end of the run')
        ]),
        pipelineTriggers([
                [$class: "GitHubPushTrigger"]
        ])
])

// We use the get[] syntax here because the first time a new branch of pipeline is loaded, the property does not exist.
node(params["BUILD_NODE"] ?: buildNodeDefault) {
    stage("Checkout Source") {
        // We want to start our pipeline in a fresh workspace since cleaning up afterwards is optional.
        // Needed because rerunning the tests on a dirty workspace fails.
        step([$class: 'WsCleanup'])
        checkout(scm)
    }

    stage("Load Variables") { // This is needed for Docker, Maven, and Sonarqube variables
        withCredentials([string(credentialsId: 'o2-artifact-project', variable: 'o2ArtifactProject')]) {
            step ([$class: "CopyArtifact",
                   projectName: o2ArtifactProject,
                   filter: "common-variables.groovy",
                   flatten: true])
        }

        load "common-variables.groovy"
    }

    stage("Build") {
        sh "gradle build -PdownloadMavenUrl=$OSSIM_MAVEN_PROXY"
        archiveArtifacts "build/libs/*.jar"
        junit "build/test-results/**/*.xml"
    }

    stage("Publish Jar") {
        withCredentials([[$class: 'UsernamePasswordMultiBinding',
                          credentialsId: 'mavenCredentials',
                          usernameVariable: 'ORG_GRADLE_PROJECT_uploadMavenRepoUsername',
                          passwordVariable: 'ORG_GRADLE_PROJECT_uploadMavenRepoPassword']]) {
            sh """
                gradle publish -PuploadMavenUrl=$OMAR_MAVEN_PROXY-snapshot
            """
        }
    }

    stage("Push Docker Image") {
        withCredentials([[$class: 'UsernamePasswordMultiBinding',
                          credentialsId: 'dockerCredentials',
                          usernameVariable: 'DOCKER_REGISTRY_USERNAME',
                          passwordVariable: 'DOCKER_REGISTRY_PASSWORD']]) {
            sh """
                docker login $DOCKER_REGISTRY_URL --username=$DOCKER_REGISTRY_USERNAME --password=$DOCKER_REGISTRY_PASSWORD
                gradle jibDockerBuild \
                    --image=$DOCKER_REGISTRY_URL/omar-volume-cleanup \
                    -Djib.from.image=${DOCKER_REGISTRY_URL}/omar-base:${getBaseImageTag()}
                docker push $DOCKER_REGISTRY_URL/omar-volume-cleanup
            """
        }
    }


    try {
        stage("Scan Code") {
            sh """
                gradle sonarqube \
                    -Dsonar.projectKey=ossimlabs_omar-volume-cleanup \
                    -Dsonar.organization=$SONARQUBE_ORGANIZATION \
                    -Dsonar.host.url=$SONARQUBE_HOST \
                    -Dsonar.login=$SONARQUBE_TOKEN \
                    ${getSonarqubeBranchArgs()}
            """
        }
    } catch (Exception e) {
        println "Code scanning failed with exception: $e"
        println "Ignoring code scans"
    }

    stage("Clean Workspace") {
        if (CLEAN_WORKSPACE == "true") step([$class: 'WsCleanup'])
    }
}

/**
 * Returns the tag for the base image. This is used to satisfy our requirements that O2 apps must output a Docker
 * image based on omar-base or omar-ossim-base in order for us to provided specialized base images for certain
 * environments.
 * Apps should pull their base image that is tagged as "release" if building on the master branch,
 * otherwise aps should use the "latest" tagged image.
 *
 * @return The Docker tag name to use when pulling the base image.
 */
String getBaseImageTag() {
    if (BRANCH_NAME == "master") return "release"
    else return "latest"
}

/**
 * Returns the command line args in "-Dproperty" form of sonar.branch.name and sonar.branch.target.
 * https://sonarcloud.io/documentation/branches/overview/
 *
 * Master and Dev are long-living branches indicated by no branch args being returned.
 * Branches starting with "hotfix" or "release" (see GitFlow) are treated as short-lived branches targeting master.
 *
 * Example return value:
 * "-Dsonar.branch.name=feature-example \
 *  -Dsonar.branch.target=dev
 *
 *  @return The -D branch properties for sonarqube (does not include a trailing slash for command line)
 */
String getSonarqubeBranchArgs() {
    // If the Jenkins pipeline is not a multi-branch pipeline we want to exclude the branch properties.
    if (env.BRANCH_NAME == null) return ""

    String args = "-Dsonar.branch.name=${BRANCH_NAME} \\"

    if (!["master", "dev"].contains(BRANCH_NAME)) { // We want to skip the target branch if on a long-living branch.
        if (BRANCH_NAME.startsWith("hotfix") || BRANCH_NAME.startsWith("release")) {
            args += "-Dsonar.branch.target=master"
        } else {
            args += "-Dsonar.branch.target=dev"
        }
    }
    return args
}