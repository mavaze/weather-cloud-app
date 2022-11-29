pipeline {
    agent any
    tools {
        jdk 'jdk11'
        maven 'maven3'
    }

    environment {
        JAVA_HOME = "${jdk}"
    }

    stages {
        stage('Prepare') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Sonar with Test Coverage') {
            steps {
                withSonarQubeEnv('sonar-server') {
                  sh 'mvn clean verify -Pcoverage'
                  script {
                        def scannerHome = tool 'sonarqube-scanner'
                        sh "${scannerHome}/bin/sonar-scanner"
                    }
                }
            }
        }
    }
}
