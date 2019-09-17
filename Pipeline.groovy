package net.opentechnology

def build() {
    node {
        stage('Clone sources') {
            git url: 'https://github.com/donaldmcintosh/triki.git'
        }

        stage('Gradle build') {
            buildInfo = rtGradle.run buildFile: 'build.gradle', tasks: 'clean build'
        }
    }
}

build()
