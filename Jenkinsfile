def getCiPipeline() {
    return {
        node {
            def workspaceDir
            env.JAVA_HOME = tool name: 'jdk-21', type: 'jdk'
            stage('Checkout Code') {
                checkout scm
            }
            stage('Setup') {
                tool name: 'jdk-21', type: 'jdk'
                sh "docker pull gradle:8.2.1-jdk17-alpine"
                sh "docker pull dtzar/helm-kubectl"
            }

            stage('Unit Test') {
                workspaceDir = sh(script: 'ls -d */|head -n 1', returnStdout: true).trim()
                dir("${env.WORKSPACE}/${workspaceDir}") {
                    try {
                        sh "chmod +x gradlew"
                        sh "./gradlew clean test --build-cache"
                    } catch (Exception e) {
                        error "Pipeline aborted due to test build failure: ${e.message}"
                    } finally {
                        jiraSendBuildInfo()
                    }
                }
            }

            // stage('Quality Gate') {
            //     workspaceDir = sh(script: 'ls -d */|head -n 1', returnStdout: true).trim()
            //     dir("${env.WORKSPACE}/${workspaceDir}") {
            //         try {
            //             withSonarQubeEnv("sonarserver") {
            //                 sh "chmod +x gradlew"
            //                 sh "./gradlew clean test sonar --build-cache"
            //             }
            //             sleep(10)
            //             def qg = waitForQualityGate()
            //             if (qg.status != 'OK') {
            //                 error "Pipeline aborted due to quality gate failure: ${qg.status}"
            //             }
            //         } catch (Exception e) {
            //             error "Pipeline aborted due to quality gate failure: ${e.message}"
            //         } finally {
            //             jiraSendBuildInfo()
            //         }
            //     }
            // }

            stage('Set Image Version') {
                workspaceDir = sh(script: 'ls -d */|head -n 1', returnStdout: true).trim()
                dir("${env.WORKSPACE}/${workspaceDir}") {
                    try {
                        env.appversion = sh(returnStdout: true, script: "./gradlew properties -q | grep 'version:' | grep -v 'kotlin.version:' | awk -F ':' '{print \$2}'").trim()
                        sh "echo ${env.appversion}"
                    } catch (Exception e) {
                        error "Pipeline aborted due to version setting failure: ${e.message}"
                    } finally {
                        jiraSendBuildInfo()
                    }
                }
            }

            stage('Set Image Name') {
                workspaceDir = sh(script: 'ls -d */|head -n 1', returnStdout: true).trim()
                dir("${env.WORKSPACE}/${workspaceDir}") {
                    try {
                        env.appname = sh(returnStdout: true, script: "docker run -v ${env.WORKSPACE}/${workspaceDir}:/app -w /app dtzar/helm-kubectl helm show chart ./charts | grep name | cut -d: -f 2 | tr -d ' '").trim() + "-${setEnv()}"
                        env.chartname = sh(returnStdout: true, script: "docker run -v ${env.WORKSPACE}/${workspaceDir}:/app -w /app dtzar/helm-kubectl helm show chart ./charts | grep name | cut -d: -f 2 | tr -d ' '").trim()
                    } catch (Exception e) {
                        error "Pipeline aborted due to image name setting failure: ${e.message}"
                    } finally {
                        jiraSendBuildInfo()
                    }
                }
            }

            stage('Build Docker Image') {
                workspaceDir = sh(script: 'ls -d */|head -n 1', returnStdout: true).trim()
                dir("${env.WORKSPACE}/${workspaceDir}") {
                    try {
                        buildAndPushDockerImage("10.0.3.224:8003/${env.appname}:${env.appversion}", 'http://10.0.3.224:8003')
                        // buildAndPushDockerImage("marimba.agencify.insure/${env.appname}:${env.appversion}", 'https://marimba.agencify.insure')
                    } catch (Exception e) {
                        error "Pipeline aborted due to docker image build failure: ${e.message}"
                    } finally {
                        jiraSendBuildInfo()
                    }
                }
            }

            stage('Package helm chart') {
                workspaceDir = sh(script: 'ls -d */|head -n 1', returnStdout: true).trim()
                dir("${env.WORKSPACE}/${workspaceDir}") {
                    try {
                        sh "sudo helm package charts/  --app-version ${env.appversion} --version ${env.appversion}"
                        withCredentials([usernamePassword(credentialsId: 'docker-registry', passwordVariable: 'pass', usernameVariable: 'user')]) {
                            sh "pwd"
                            sh "ls -l '${env.chartname}-${env.appversion}.tgz'"
                            sh "curl -u docker-registry:$pass http://10.0.3.224:8002/repository/agencify-helm-repo/ --upload-file ${env.chartname}-${env.appversion}.tgz -v"
                        }
                    } catch (Exception e) {
                        error "Pipeline aborted due to helm chart packaging failure: ${e.message}"
                    } finally {
                        jiraSendBuildInfo()
                    }
                }
            }

            stage('Deploy') {

                def K8_ENV = setEnv()
                def K8_ENV_SECRET = getK8Secret()
                def K8_API = setK8Api()
                def K8_DOMAIN = setK8Domain()
                def K8_ROUTE = setK8Route()
                if (K8_ENV != 'prod') {
                    sshagent(credentials: ['agencify-backend']) {
                        try {
                            if (K8_ENV == 'dev') {
                                sh "ssh ubuntu@10.0.3.74 'cd /home/ubuntu/agencify-backend && git fetch && git checkout ${env.BRANCH_NAME} && git pull origin ${env.BRANCH_NAME} --rebase && sudo docker compose up -d'"
                                jiraSendDeploymentInfo environmentId: 'development', environmentName: 'development', environmentType: 'development'
                            } else if (K8_ENV == 'staging') {
                                sh "ssh ubuntu@10.0.3.85 'cd /home/ubuntu/agencify-backend && git fetch && git checkout ${env.BRANCH_NAME} && git pull origin ${env.BRANCH_NAME} --rebase && sudo docker compose -f staging-compose.yml up -d'"
                                jiraSendDeploymentInfo environmentId: 'staging', environmentName: 'staging', environmentType: 'staging'
                            }
                        } catch (Exception e) {
                            error "Pipeline aborted due to deployment failure: ${e.message}"
                        } finally {
                            jiraSendBuildInfo()
                        }
                    }
                }
                if (K8_ENV == 'prod') {
                    workspaceDir = sh(script: 'ls -d */|head -n 1', returnStdout: true).trim()
                    dir("${env.WORKSPACE}/${workspaceDir}") {
                        try {
                            withKubeCredentials(kubectlCredentials: [[credentialsId: "$K8_ENV_SECRET", serverUrl: "$K8_API"]]) {
                                sh "helm list -n $K8_ENV"
                                sh "helm upgrade --install --set image.repository='10.0.3.224:8003/${env.appname}:${env.appversion}' --set image.tag='${env.appversion}' --set ingress.domain='$K8_DOMAIN' --set ingress.route='$K8_ROUTE' ${env.appname} charts/ -n $K8_ENV"
                            }
                        } catch (Exception e) {
                            error "Pipeline aborted due to deployment failure: ${e.message}"
                        } finally {
                            jiraSendBuildInfo()
                            jiraSendDeploymentInfo environmentId: 'production', environmentName: 'production', environmentType: 'production'
                        }
                    }
                }

            }

        }

    }
}

def buildAndPushDockerImage(String imageName, String registryUrl) {
    def app = docker.build(imageName)
    docker.withRegistry(registryUrl, 'docker-registry') {
        app.push("${env.appversion}")
        app.push("latest")
    }
}

def setEnv() {
//    sh(returnStdout: true, script: "git config --global --add safe.directory '*'")
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
//    sh(returnStdout: true, script: "git config --global --add safe.directory '*'")
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains(Constants.PRODUCTION_TAG)) {
        return 'https://10.0.4.212:6443'
    } else {
        switch (env.BRANCH_NAME) {
            case Constants.PRODUCTION_BRANCH:
                return 'https://10.0.4.212:6443'
            case Constants.PREPROD_BRANCH:
                return 'https://10.0.4.212:6443'
            case Constants.MASTER_BRANCH:
                return 'https://10.0.3.149:6443'
            case Constants.DEVELOP_BRANCH:
                return 'https://10.0.3.149:6443'
            default:
                return 'https://10.0.3.149:6443'
        }
    }
}

def setK8Domain() {
//    sh(returnStdout: true, script: "git config --global --add safe.directory '*'")
    def ENV_TAG = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    if (ENV_TAG.contains(Constants.PRODUCTION_TAG)) {
        return 'bozomu.agencify.insure'
    } else {
        switch (env.BRANCH_NAME) {
            case Constants.PRODUCTION_BRANCH:
                return 'bozomu.agencify.insure'
            case Constants.PREPROD_BRANCH:
                return 'bozomu.agencify.insure'
            case Constants.MASTER_BRANCH:
                return 'janzi.agencify.insure'
            case Constants.DEVELOP_BRANCH:
                return 'janzi.agencify.insure'
            default:
                return 'janzi.agencify.insure'
        }
    }
}

def setK8Route() {
//    sh(returnStdout: true, script: "git config --global --add safe.directory '*'")
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
            case Constants.DEVELOP_BRANCH:
                return '/api'
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
