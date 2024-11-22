#!/usr/bin/env groovy

pipeline {
    agent any
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        disableConcurrentBuilds()
        timeout(time: 1, unit: 'HOURS')
    }
    
    environment {
        WORKSPACE_DIR = sh(script: 'pwd', returnStdout: true).trim()
        SONAR_TOKEN = 'sqp_0328fe551ffc7bcbb25740fffe0cf3d254b478b7'
        SONAR_URL = 'https://obokano.agencify.insure/'
        PROJECT_KEY = 'agencify-auth'
        DOCKER_REGISTRY = '10.0.3.224:8003'
        HELM_REPO = 'http://10.0.3.224:8002/repository/agencify-helm-repo/'
        BUILD_TIMESTAMP = sh(script: 'date +%Y%m%d_%H%M%S', returnStdout: true).trim()
    }
    
    stages {
        stage("Build and Deploy") {
            steps {
                script {
                    def k8sEnv = setEnv()
                    echo "Building for environment: ${k8sEnv}"
                    
                    withCredentials([usernameColonPassword(credentialsId: 'docker-registry', variable: 'DOCKER_CREDS')]) {
                        def dockerUser = sh(script: "echo $DOCKER_CREDS | cut -d':' -f1", returnStdout: true).trim()
                        def dockerPass = sh(script: "echo $DOCKER_CREDS | cut -d':' -f2", returnStdout: true).trim()
                        
                        try {
                            ansiblePlaybook(
                                credentialsId: 'dev-server',
                                disableHostKeyChecking: true,
                                installation: 'ansible',
                                inventory: 'dev.inv',
                                playbook: 'deploy-playbook.yml',
                                extras: """-e 'workspace_dir=${env.WORKSPACE_DIR}' 
                                         -e 'sonar_token=${env.SONAR_TOKEN}' 
                                         -e 'sonar_url=${env.SONAR_URL}' 
                                         -e 'project_key=${env.PROJECT_KEY}'
                                         -e 'docker_registry=${env.DOCKER_REGISTRY}'
                                         -e 'docker_user=${dockerUser}'
                                         -e 'docker_pass=${dockerPass}'
                                         -e 'helm_repo=${env.HELM_REPO}'
                                         -e 'env_type=${k8sEnv}'
                                         -e 'k8s_env_secret=${getK8Secret()}'
                                         -e 'k8s_api=${setK8Api()}'
                                         -e 'k8s_domain=${setK8Domain()}'
                                         -e 'k8s_route=${setK8Route()}'"""
                            )
                        } catch (Exception e) {
                            currentBuild.result = 'FAILURE'
                            error "Pipeline failed: ${e.message}"
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}

def setEnv() {
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains(Constants.PRODUCTION_TAG)) {
        return 'prod'
    } else {
        switch (env.BRANCH_NAME) {
            case Constants.PRODUCTION_BRANCH:
                return 'prod'
            case Constants.PREPROD_BRANCH:
                return 'preprod'
            case Constants.MASTER_BRANCH:
                return 'staging'
            case Constants.DEVELOP_BRANCH:
                return 'dev'
            default:
                return 'dev'
        }
    }
}

def getK8Secret() {
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains(Constants.PRODUCTION_TAG)) {
        return 'k8sonprodNS-prod'
    } else {
        switch (env.BRANCH_NAME) {
            case Constants.PRODUCTION_BRANCH:
                return 'k8sonprodNS-prod'
            case Constants.PREPROD_BRANCH:
                return 'k8sonPreprodNS-Preprod'
            case Constants.MASTER_BRANCH:
                return 'k8sonNewDevNS-Staging'
            case Constants.DEVELOP_BRANCH:
                return 'k8sonNewDevNS-Dev'
            default:
                return 'k8sonNewDevNS-Dev'
        }
    }
}

def setK8Api() {
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains(Constants.PRODUCTION_TAG)) {
        return 'https://10.0.4.212:6443'
    } else {
        switch (env.BRANCH_NAME) {
            case Constants.PRODUCTION_BRANCH:
                return 'https://10.0.4.212:6443'
            case Constants.PREPROD_BRANCH:
                return 'https://10.0.4.212:6443'
            case Constants.MASTER_BRANCH:
                return 'https://10.0.3.149:6443'
            case Constants.DEVELOP_BRANCH:
                return 'https://10.0.3.149:6443'
            default:
                return 'https://10.0.3.149:6443'
        }
    }
}

def setK8Domain() {
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains(Constants.PRODUCTION_TAG)) {
        return 'bozomu.agencify.insure'
    } else {
        switch (env.BRANCH_NAME) {
            case Constants.PRODUCTION_BRANCH:
                return 'bozomu.agencify.insure'
            case Constants.PREPROD_BRANCH:
                return 'bozomu.agencify.insure'
            case Constants.MASTER_BRANCH:
                return 'janzi.agencify.insure'
            case Constants.DEVELOP_BRANCH:
                return 'janzi.agencify.insure'
            default:
                return 'janzi.agencify.insure'
        }
    }
}

def setK8Route() {
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains(Constants.PRODUCTION_TAG)) {
        return '/prod'
    } else {
        switch (env.BRANCH_NAME) {
            case Constants.PRODUCTION_BRANCH:
                return '/prod'
            case Constants.PREPROD_BRANCH:
                return '/preprod'
            case Constants.MASTER_BRANCH:
                return '/staging/v1'
            case Constants.DEVELOP_BRANCH:
                return '/api'
            default:
                return '/api'
        }
    }
}

class Constants {
    static final String MASTER_BRANCH = 'master'
    static final String DEVELOP_BRANCH = 'develop'
    static final String PREPROD_BRANCH = 'preprod'
    static final String PRODUCTION_BRANCH = 'production'
    static final String PRODUCTION_TAG = '-rc'
}




