#!groovy

node() {
    def mvnHome

    stage('Init&Checkout') {
        cleanWs()
        checkout scm

        mvnHome = tool 'maven'
    }

    stage('B-Build') {
        sh "${mvnHome}/bin/mvn clean package -Dmaven.test.skip=true"
    }


    /*stage('Sonar') {
        withSonarQubeEnv('sonarServer') {
            sh "${mvnHome}/bin/mvn sonar:sonar"
        }

        timeout(10) {
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
                error "Pipeline aborted due to quality gate failure: ${qg.status}"
            }else{
                echo '恭喜你通过了代码的质量检查！'
            }
        }
    }*/

    stage('deploy') {
        sh "fab -f /home/jenkins/jenkins_python/deploy_dev.py deploy_dev:hosts='172.28.50.206',workspace=${workspace}"
    }
}
