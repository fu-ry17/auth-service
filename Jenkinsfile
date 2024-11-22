pipeline{
    
    agent any
    
    environment {
      DOCKER_TAG = get_version()
    }
    
    stages{
        stage("Checkout code "){
            steps{
              checkout scm
            }
        }
        
        stage("Run ansible playbook"){
          steps{
             ansiblePlaybook credentialsId: 'dev-server', 
                disableHostKeyChecking: true, extras: "", 
                installation: 'ansible', inventory: 'dev.inv', 
                playbook: 'deploy-docker.yml', vaultTmpPath: ''
          }
        }   
      
    }
}


def get_version(){
  def commitHash = sh returnStdout: true, script: '''git rev-parse --short HEAD'''
  return commitHash
}




