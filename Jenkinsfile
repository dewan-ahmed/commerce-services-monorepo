pipeline {
    agent any

    stages {
        stage('Build and Test') {
            steps {
                sh 'mvn --batch-mode clean verify'
            }
        }
    }

    post {
        always {
            junit testResults: '**/target/surefire-reports/TEST-*.xml', allowEmptyResults: true
        }
        success {
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
        }
    }
}
