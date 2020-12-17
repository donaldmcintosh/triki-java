pipeline{
    agent any
    stages {
        stage('Clone sources') {
	    steps {
                git credentialsId: 'jenkins-robin', url: 'git@github.com:donaldmcintosh/triki.git'
	    }
        }
        stage('Gradle build') {
	    steps {
                sh "./gradlew clean build publishToMavenLocal"
	        cucumber fileIncludePattern: 'build/dev.json'
	    }
        }
        stage('Deploy local') {
	    steps{
	        // Need env param LOCAL_DEPLOY set for this 
                sh "./gradlew unzipDistrib"
	    }
        }
        stage('Release'){
	    steps{
                timeout(time: 10, unit: 'MINUTES') {
                    input message: "Release?", ok: "Yes"
                }
		// release cannot get credentieals for git unless they are in ~jenkins/.ssh
		// And need a ~/.gitconfig with user details under jenkins user
                sh "git clean -fd; ./gradlew release -Prelease.useAutomaticVersion=true"
	    }
        }
    }
}
