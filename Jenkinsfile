@Library('jenkins-shared-library') _

pipeline {
    agent any
    
    environment {
        WORKSPACE_DIR = sh(script: 'pwd', returnStdout: true).trim()
        DOCKER_REGISTRY = '10.0.3.224:8003'
        HELM_REPO = 'http://10.0.3.224:8002/repository/agencify-helm-repo/'
    }
    
    stages {
        stage('Build and Deploy') {
            steps {
                script {
                    def k8sEnv = setEnv()
                    def k8sDomain = setK8Domain()
                    def k8sRoute = setK8Route()
                    
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
                            extras: """-e 'workspace_dir=${env.WORKSPACE_DIR}' 
                                     -e 'docker_registry=${env.DOCKER_REGISTRY}'
                                     -e 'docker_user=${dockerUser}'
                                     -e 'docker_pass=${dockerPass}'
                                     -e 'helm_repo=${env.HELM_REPO}'
                                     -e 'env_type=${k8sEnv}'
                                     -e 'k8s_env=${k8sEnv}'
                                     -e 'k8s_domain=${k8sDomain}'
                                     -e 'k8s_route=${k8sRoute}'"""
                        )
                    }
                }
            }
        }
    }
    
    post {
        always {
            jiraSendBuildInfo()
        }
        success {
            script {
                def envType = setEnv()
                if (envType == 'prod') {
                    jiraSendDeploymentInfo environmentId: 'production', 
                                         environmentName: 'production', 
                                         environmentType: 'production'
                } else if (envType == 'staging') {
                    jiraSendDeploymentInfo environmentId: 'staging', 
                                         environmentName: 'staging', 
                                         environmentType: 'staging'
                } else {
                    jiraSendDeploymentInfo environmentId: 'development', 
                                         environmentName: 'development', 
                                         environmentType: 'development'
                }
            }
        }
    }
}



