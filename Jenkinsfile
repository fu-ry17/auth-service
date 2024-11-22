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
                script {
                    echo "Workspace directory: ${env.WORKSPACE_DIR}"
                    ansiblePlaybook(
                        credentialsId: 'dev-server',
                        disableHostKeyChecking: true,
                        installation: 'ansible',
                        inventory: 'dev.inv',
                        playbook: 'deploy-playbook.yml',
                        extras: "-e 'workspace_dir=${env.WORKSPACE_DIR}'"
                    )
                }
            }
        }
    }
}



