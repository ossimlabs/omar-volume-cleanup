//noinspection GroovyAssignabilityCheck
properties([
        parameters([
                string(name: 'BUILD_NODE', defaultValue: 'omar-build', description: 'The build node to run on'),
                booleanParam(name: 'CLEAN_WORKSPACE', defaultValue: true, description: 'Clean the workspace at the end of the run')
        ]),
        pipelineTriggers([
                [$class: "GitHubPushTrigger"]
        ])
])

node("${BUILD_NODE}") {
    stage("Checkout Source") {
        checkout(scm)
    }

    stage("Load Variables") { // This is needed for Docker, Maven, and Sonarqube variables
        step([$class     : "CopyArtifact",
              projectName: "ossim-ci",
              filter     : "common-variables.groovy",
              flatten    : true])

        load "common-variables.groovy"
    }

    stage("Build") {
        sh "gradle build -PdownloadMavenUrl=$OSSIM_MAVEN_PROXY"
        archiveArtifacts "build/libs/*.jar"
    }

    stage("Publish Jar") {
        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                          credentialsId   : 'nexusCredentials',
                          usernameVariable: 'ORG_GRADLE_PROJECT_uploadMavenRepoUsername',
                          passwordVariable: 'ORG_GRADLE_PROJECT_uploadMavenRepoPassword']]) {
            sh """
            gradle publish -PuploadMavenUrl=$OMAR_MAVEN_PROXY-snapshot
            """
        }
    }

    stage("Push Docker Image") {
        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                          credentialsId   : 'dockerCredentials',
                          usernameVariable: 'DOCKER_REGISTRY_USERNAME',
                          passwordVariable: 'DOCKER_REGISTRY_PASSWORD']]) {
            sh """
            docker login $DOCKER_REGISTRY_URL --username=$DOCKER_REGISTRY_USERNAME --password=$DOCKER_REGISTRY_PASSWORD
            gradle jibDockerBuild --image=$DOCKER_REGISTRY_URL/omar-volume-cleanup
            docker push $DOCKER_REGISTRY_URL/omar-volume-cleanup
            """
        }
    }

    stage("Code Scans") {
        withSonarQubeEnv(SONARQUBE_NAME) {
            sh """
            gradle sonarqube \
                -Dsonar.projectKey=ossimlabs_omar-volume-cleanup \
                -Dsonar.organization=ossimlabs \
            """
        }
    }

    stage("Clean Workspace") {
        if (CLEAN_WORKSPACE == "true") step([$class: 'WsCleanup'])
    }
}