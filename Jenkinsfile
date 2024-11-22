pipeline {
    agent any
    
    environment {
        DOCKER_TAG = get_version()
        WORKSPACE_DIR = sh(script: 'pwd', returnStdout: true).trim()
        SONAR_TOKEN = 'sqp_0328fe551ffc7bcbb25740fffe0cf3d254b478b7'
        SONAR_URL = 'https://obokano.agencify.insure/'
        PROJECT_KEY = 'agencify-auth'
    }
    
    stages {
        stage("Checkout code") {
            steps {
                checkout scm
            }
        }
        
        stage("Deploy with Ansible") {
            steps {
                script {
                    echo "Workspace directory: ${env.WORKSPACE_DIR}"
                    ansiblePlaybook(
                        credentialsId: 'dev-server',
                        disableHostKeyChecking: true,
                        installation: 'ansible',
                        inventory: 'dev.inv',
                        playbook: 'deploy-playbook.yml',
                        extras: """-e 'workspace_dir=${env.WORKSPACE_DIR}' 
                                 -e 'sonar_token=${env.SONAR_TOKEN}' 
                                 -e 'sonar_url=${env.SONAR_URL}'
                                 -e 'project_key=${env.PROJECT_KEY}'"""
                    )
                }
            }
        }
    }
}

def get_version() {
    def commitHash = sh(returnStdout: true, script: 'git rev-parse --short HEAD')
    return commitHash
}




