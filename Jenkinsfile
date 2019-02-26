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
    stage("Checkout source") {
        checkout(scm)
    }

    stage("Load Variables") {
        step([$class     : "CopyArtifact",
              projectName: "ossim-ci",
              filter     : "common-variables.groovy",
              flatten    : true])

        load "common-variables.groovy"
    }

    stage("Build") {
        sh "gradle build"
        archiveArtifacts "build/libs/*.jar"
    }

    stage("Publish Jar") {
        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                          credentialsId   : 'nexusCredentials',
                          usernameVariable: 'ORG_GRADLE_PROJECT_mavenRepoUsername',
                          passwordVariable: 'ORG_GRADLE_PROJECT_mavenRepoPassword']]) {
            sh """
            gradle publish
            """
        }
    }

    stage("Publish Docker App") {
        withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                          credentialsId   : 'dockerCredentials',
                          usernameVariable: 'DOCKER_REGISTRY_USERNAME',
                          passwordVariable: 'DOCKER_REGISTRY_PASSWORD']]) {
            // Run all tasks on the app. This includes pushing to OpenShift and S3.
            sh """
            gradle jibDockerBuild --image=$DOCKER_REGISTRY_URL/omar-volume-cleanup
            docker push $DOCKER_REGISTRY_URL/omar-volume-cleanup
            """
        }
    }

    stage("Clean Workspace") {
        if (CLEAN_WORKSPACE == "true") step([$class: 'WsCleanup'])
    }
}