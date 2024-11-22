pipeline {
    agent any
    
    environment {
        WORKSPACE_DIR = sh(script: 'pwd', returnStdout: true).trim()
        SONAR_TOKEN = 'sqp_0328fe551ffc7bcbb25740fffe0cf3d254b478b7'
        SONAR_URL = 'https://obokano.agencify.insure/'
        PROJECT_KEY = 'agencify-auth'
        DOCKER_REGISTRY = '10.0.3.224:8003'
        HELM_REPO = 'http://10.0.3.224:8002/repository/agencify-helm-repo/'
        SSH_KEY = 'ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIKk3ZpAx/6kxaKvPlqby5XSzWWl8ljlHPcFN7gr+PrRQ brian.yewa@agencify.insure'
    }
    
    stages {
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
                                     -e 'docker_registry=${env.DOCKER_REGISTRY}'
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
                    def branchName = env.BRANCH_NAME ?: 'develop'
                    echo "Deploying to environment: ${envType} using branch: ${branchName}"
                    
                    withCredentials([
                        usernameColonPassword(credentialsId: 'docker-registry', variable: 'DOCKER_CREDS'),
                        sshUserPrivateKey(credentialsId: 'dev-ssh-key', keyFileVariable: 'SSH_KEY')
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
                                     -e 'docker_registry=${env.DOCKER_REGISTRY}'
                                     -e 'ssh_key=${SSH_KEY}'"""
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




