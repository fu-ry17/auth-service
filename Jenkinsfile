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
            sh 'ansible-playbook deploy-playbook.yml -i inventory/dev'
          }
        }   
      
    }
}


def get_version(){
  def commitHash = sh returnStdout: true, script: '''git rev-parse --short HEAD'''
  return commitHash
}




