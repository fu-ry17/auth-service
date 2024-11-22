pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = '10.0.3.224:8003'
        HELM_REPO = 'http://10.0.3.224:8002/repository/agencify-helm-repo/'
        SONAR_TOKEN = 'sqp_0328fe551ffc7bcbb25740fffe0cf3d254b478b7'
        SONAR_URL = 'https://obokano.agencify.insure/'
        PROJECT_KEY = 'agencify-auth'
        BRANCH_NAME = "${env.BRANCH_NAME ?: 'develop'}"
    }

    stages {
        stage('Checkout Code') {
            steps {
                echo "Checking out code from repository..."
                checkout scm
                echo "Code checkout complete"
            }
        }

        stage('Run Ansible Pipeline') {
            steps {
                script {
                    echo "Starting Ansible pipeline execution..."
                    def workspaceDir = sh(script: 'pwd', returnStdout: true).trim()
                    echo "Current branch: ${BRANCH_NAME}"
                    
                    echo "Determining environment configuration..."
                    def K8_ENV = setEnv()
                    def K8_ENV_SECRET = getK8Secret()
                    def K8_API = setK8Api()
                    def K8_DOMAIN = setK8Domain()
                    def K8_ROUTE = setK8Route()

                    echo """
                    Environment Configuration:
                    - Branch Name: ${BRANCH_NAME}
                    - Environment: ${K8_ENV}
                    - Secret: ${K8_ENV_SECRET}
                    - API: ${K8_API}
                    - Domain: ${K8_DOMAIN}
                    - Route: ${K8_ROUTE}
                    """

                    echo "Getting Docker credentials..."
                    withCredentials([
                        usernameColonPassword(credentialsId: 'docker-registry', variable: 'DOCKER_CREDS')
                    ]) {
                        def dockerUser = sh(script: "echo $DOCKER_CREDS | cut -d':' -f1", returnStdout: true).trim()
                        def dockerPass = sh(script: "echo $DOCKER_CREDS | cut -d':' -f2", returnStdout: true).trim()
                        
                        echo "Running Ansible playbook..."
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
                                     -e 'branch_name=${BRANCH_NAME}'
                                     -e 'sonar_token=${SONAR_TOKEN}'
                                     -e 'sonar_url=${SONAR_URL}'
                                     -e 'project_key=${PROJECT_KEY}'"""
                        )
                        echo "Ansible playbook execution completed"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline executed successfully!"
        }
        failure {
            echo "Pipeline failed! Check the logs for details."
        }
        always {
            echo "Pipeline execution completed. Status: ${currentBuild.result}"
        }
    }
}

def setEnv() {
    echo "Determining environment based on git tag and branch..."
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    echo "Found git tag: ${ENV_TAG}"
    
    if (ENV_TAG.contains('-rc')) {
        echo "RC tag found, setting environment to prod"
        return 'prod'
    } else {
        echo "Determining environment from branch: ${env.BRANCH_NAME}"
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



