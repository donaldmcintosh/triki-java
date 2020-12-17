package net.opentechnology

def build() {
    node {
        stage('Clone sources') {
            git credentialsId: 'jenkins-robin', url: 'git@github.com:donaldmcintosh/triki.git'
        }

        stage('Gradle build') {
            sh "./gradlew clean build publishToMavenLocal"
	    cucumber fileIncludePattern: 'build/dev.json'
        }

        stage('Deploy local') {
            sh "./gradlew unzipDistrib"
        }

        stage('Release'){
            timeout(time: 10, unit: 'MINUTES') {
                input message: "Release?", ok: "Yes"
            }
	    sshagent (credentials: ['jenkins-robin']) {
               sh "git clean -fd; ./gradlew release -Prelease.useAutomaticVersion=true"
            }
        }
    }
}

build()
