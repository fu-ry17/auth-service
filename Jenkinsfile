pipeline {
    agent any
    
    environment {
        WORKSPACE_DIR = sh(script: 'pwd', returnStdout: true).trim()
        SONAR_TOKEN = 'sqp_0328fe551ffc7bcbb25740fffe0cf3d254b478b7'
        SONAR_URL = 'https://obokano.agencify.insure/'
        PROJECT_KEY = 'agencify-auth'
        DOCKER_REGISTRY = '10.0.3.224:8003'
    }
    
    stages {
        stage("Build and Test") {
            steps {
                script {
                    def branchName = 'dev'
                    echo "Building for branch: ${branchName}"
                    
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
                                     -e 'env_type=${branchName}'"""
                        )
                    }
                }
            }
        }
    }
}




