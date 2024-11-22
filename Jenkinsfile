pipeline {
    agent any
    
    environment {
        DOCKER_TAG = get_version()
        WORKSPACE_DIR = sh(script: 'pwd', returnStdout: true).trim()
    }
    
    stages {
        stage("Checkout code") {
            steps {
                checkout scm
            }
        }
        
        stage("Deploy with Ansible") {
            steps {
                ansiblePlaybook(
                    credentialsId: 'dev-server',
                    disableHostKeyChecking: true,
                    installation: 'ansible',
                    inventory: 'dev.inv',
                    playbook: 'deploy-playbook.yml',
                    extras: "-e workspace_dir=${env.WORKSPACE_DIR}"
                )
            }
        }
    }
}

def get_version() {
    def commitHash = sh(returnStdout: true, script: 'git rev-parse --short HEAD')
    return commitHash
}




