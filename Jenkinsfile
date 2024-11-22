pipeline {
    agent any
    
    environment {
        WORKSPACE_DIR = sh(script: 'pwd', returnStdout: true).trim()
        SONAR_TOKEN = 'sqp_0328fe551ffc7bcbb25740fffe0cf3d254b478b7'
        SONAR_URL = 'https://obokano.agencify.insure/'
        PROJECT_KEY = 'agencify-auth'
        DOCKER_REGISTRY = '10.0.3.224:8003'
        HELM_REPO = 'http://10.0.3.224:8002/repository/agencify-helm-repo/'
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage("Test, Build and Package") {
            steps {
                script {
                    def envType = setEnv()
                    echo "Building for environment: ${envType}"
                    
                    withCredentials([usernameColonPassword(credentialsId: 'docker-registry', variable: 'DOCKER_CREDS')]) {
                        def dockerUser = sh(script: "echo $DOCKER_CREDS | cut -d':' -f1", returnStdout: true).trim()
                        def dockerPass = sh(script: "echo $DOCKER_CREDS | cut -d':' -f2", returnStdout: true).trim()
                        
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
                                     -e 'docker_registry=${Constants.DOCKER_REGISTRY}'
                                     -e 'docker_user=${dockerUser}'
                                     -e 'docker_pass=${dockerPass}'
                                     -e 'helm_repo=${env.HELM_REPO}'
                                     -e 'env_type=${envType}'"""
                        )
                    }
                }
            }
        }

        stage('Deploy to Environment') {
            steps {
                script {
                    def envType = setEnv()
                    def branchName = env.BRANCH_NAME ?: Constants.DEVELOP_BRANCH
                    echo "Deploying to environment: ${envType} using branch: ${branchName}"
                    
                    withCredentials([
                        usernameColonPassword(credentialsId: 'docker-registry', variable: 'DOCKER_CREDS')
                    ]) {
                        def dockerUser = sh(script: "echo $DOCKER_CREDS | cut -d':' -f1", returnStdout: true).trim()
                        def dockerPass = sh(script: "echo $DOCKER_CREDS | cut -d':' -f2", returnStdout: true).trim()
                        
                        ansiblePlaybook(
                            credentialsId: 'ubuntu-dev',
                            disableHostKeyChecking: true,
                            installation: 'ansible',
                            inventory: 'host.inv',
                            playbook: 'environment-deployment.yml',
                            extras: """-e 'env_type=${envType}'
                                     -e 'branch_name=${branchName}'
                                     -e 'app_name=${env.app_name}'
                                     -e 'app_version=${env.app_version}'
                                     -e 'k8s_secret=${getK8Secret()}'
                                     -e 'k8s_api=${setK8Api()}'
                                     -e 'k8s_domain=${setK8Domain()}'
                                     -e 'k8s_route=${setK8Route()}'
                                     -e 'docker_registry=${Constants.DOCKER_REGISTRY}'"""
                        )
                    }
                }
            }
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
        return Constants.K8_API ?: 'https://10.0.4.212:6443'
    } else {
        switch (env.BRANCH_NAME) {
            case Constants.PRODUCTION_BRANCH:
            case Constants.PREPROD_BRANCH:
                return 'https://10.0.4.212:6443'
            default:
                return 'https://10.0.3.149:6443'
        }
    }
}

def setK8Domain() {
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains(Constants.PRODUCTION_TAG) || env.BRANCH_NAME in [Constants.PRODUCTION_BRANCH, Constants.PREPROD_BRANCH]) {
        return 'bozomu.agencify.insure'
    }
    return 'janzi.agencify.insure'
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
            default:
                return '/api'
        }
    }
}

class Constants {
    String ENV_TAG = ''
    static final String MASTER_BRANCH = 'master'
    static final String DEVELOP_BRANCH = 'develop'
    static final String PREPROD_BRANCH = 'preprod'
    static final String PRODUCTION_BRANCH = 'production'
    static final String PRODUCTION_TAG = '-rc'

    static final String DOCKER_REGISTRY = '10.0.3.224:8003'
    static final String K8_ENV = ''
    static final String K8_ENV_SECRET = ''
    static final String K8_API = ''
}




