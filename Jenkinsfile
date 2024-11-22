pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = '10.0.3.224:8003'
        HELM_REPO = 'http://10.0.3.224:8002/repository/agencify-helm-repo/'
        SONAR_TOKEN = 'sqp_0328fe551ffc7bcbb25740fffe0cf3d254b478b7'
        SONAR_URL = 'https://obokano.agencify.insure/'
        PROJECT_KEY = 'agencify-auth'
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Run Ansible Pipeline') {
            steps {
                script {
                    def workspaceDir = sh(script: 'pwd', returnStdout: true).trim()
                    
                    def K8_ENV = setEnv()
                    def K8_ENV_SECRET = getK8Secret()
                    def K8_API = setK8Api()
                    def K8_DOMAIN = setK8Domain()
                    def K8_ROUTE = setK8Route()

                    withCredentials([
                        usernameColonPassword(credentialsId: 'docker-registry', variable: 'DOCKER_CREDS')
                    ]) {
                        def dockerUser = sh(script: "echo $DOCKER_CREDS | cut -d':' -f1", returnStdout: true).trim()
                        def dockerPass = sh(script: "echo $DOCKER_CREDS | cut -d':' -f2", returnStdout: true).trim()
                        
                        ansiblePlaybook(
                            credentialsId: 'dev-server',
                            disableHostKeyChecking: true,
                            installation: 'ansible',
                            inventory: 'dev.inv',
                            playbook: 'deploy-playbook.yml',
                            extras: """-e 'workspace_dir=${workspaceDir}' 
                                     -e 'docker_registry=${DOCKER_REGISTRY}'
                                     -e 'docker_user=${dockerUser}'
                                     -e 'docker_pass=${dockerPass}'
                                     -e 'helm_repo=${HELM_REPO}'
                                     -e 'env_type=${K8_ENV}'
                                     -e 'k8s_env=${K8_ENV}'
                                     -e 'k8s_secret=${K8_ENV_SECRET}'
                                     -e 'k8s_api=${K8_API}'
                                     -e 'k8s_domain=${K8_DOMAIN}'
                                     -e 'k8s_route=${K8_ROUTE}'
                                     -e 'branch_name=${env.BRANCH_NAME}'
                                     -e 'sonar_token=${SONAR_TOKEN}'
                                     -e 'sonar_url=${SONAR_URL}'
                                     -e 'project_key=${PROJECT_KEY}'"""
                        )
                    }
                }
            }
        }
    }
}

def setEnv() {
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains('-rc')) {
        return 'prod'
    } else {
        switch (env.BRANCH_NAME) {
            case 'production':
                return 'prod'
            case 'preprod':
                return 'preprod'
            case 'master':
                return 'staging'
            case 'develop':
                return 'dev'
            default:
                return 'dev'
        }
    }
}

def getK8Secret() {
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains('-rc')) {
        return 'k8sonprodNS-prod'
    } else {
        switch (env.BRANCH_NAME) {
            case 'production':
                return 'k8sonprodNS-prod'
            case 'preprod':
                return 'k8sonPreprodNS-Preprod'
            case 'master':
                return 'k8sonNewDevNS-Staging'
            case 'develop':
                return 'k8sonNewDevNS-Dev'
            default:
                return 'k8sonNewDevNS-Dev'
        }
    }
}

def setK8Api() {
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains('-rc')) {
        return 'https://10.0.4.212:6443'
    } else {
        switch (env.BRANCH_NAME) {
            case 'production':
            case 'preprod':
                return 'https://10.0.4.212:6443'
            default:
                return 'https://10.0.3.149:6443'
        }
    }
}

def setK8Domain() {
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains('-rc')) {
        return 'bozomu.agencify.insure'
    } else {
        switch (env.BRANCH_NAME) {
            case 'production':
            case 'preprod':
                return 'bozomu.agencify.insure'
            default:
                return 'janzi.agencify.insure'
        }
    }
}

def setK8Route() {
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains('-rc')) {
        return '/prod'
    } else {
        switch (env.BRANCH_NAME) {
            case 'production':
                return '/prod'
            case 'preprod':
                return '/preprod'
            case 'master':
                return '/staging/v1'
            default:
                return '/api'
        }
    }
}



