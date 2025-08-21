pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from repository...'
                git branch: 'main', url: 'https://github.com/helal6651/Digital-bank.git'
            }
        }
        
        stage('Build Services') {
            parallel {
                stage('Build Common Service') {
                    steps {
                        echo 'Building common-service...'
                        dir('services/common-service') {
                            bat './gradlew clean build -x test'
                        }
                    }
                }
                
                stage('Build User Service') {
                    steps {
                        echo 'Building user-service...'
                        dir('services/user-service') {
                            bat './gradlew clean build -x test'
                        }
                    }
                }
                
                stage('Build Account Service') {
                    steps {
                        echo 'Building account-service...'
                        dir('services/account-service') {
                            bat './mvnw clean package -DskipTests'
                        }
                    }
                }
                
                stage('Build Frontend') {
                    steps {
                        echo 'Building frontend...'
                        dir('frontend') {
                            bat 'npm install'
                            bat 'npm run build'
                        }
                    }
                }
            }
        }
        
        stage('Docker Login') {
            steps {
                echo 'Logging into DockerHub...'
                script {
                    // Manual Docker login - you'll need to enter credentials interactively
                    echo 'Please ensure Docker is logged in to DockerHub'
                    bat 'docker info'
                }
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('User Service Docker') {
                    steps {
                        echo 'Building user-service Docker image...'
                        dir('services/user-service') {
                            bat 'docker build -t bjitpdmad/user-service:latest .'
                        }
                    }
                }
                
                stage('Account Service Docker') {
                    steps {
                        echo 'Building account-service Docker image...'
                        dir('services/account-service') {
                            bat 'docker build -t bjitpdmad/account-service:latest .'
                        }
                    }
                }
                
                stage('Frontend Docker') {
                    steps {
                        echo 'Building frontend Docker image...'
                        dir('frontend') {
                            bat 'docker build -t bjitpdmad/db-frontend:latest .'
                        }
                    }
                }
            }
        }
        
        stage('Push Docker Images') {
            steps {
                echo 'Pushing Docker images to DockerHub...'
                script {
                    bat 'docker push bjitpdmad/user-service:latest'
                    bat 'docker push bjitpdmad/account-service:latest'
                    bat 'docker push bjitpdmad/db-frontend:latest'
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                echo 'Deploying to KIND cluster using host kubectl...'
                script {
                    bat '''
                        powershell -ExecutionPolicy Bypass -Command "
                            Write-Host 'Setting up environment...'
                            $currentDir = Get-Location
                            Write-Host 'Current directory:' $currentDir
                            
                            Write-Host 'Navigating to Kubernetes overlays directory...'
                            Set-Location 'kubernetes\\overlays\\prod'
                            
                            Write-Host 'Applying Kubernetes manifests...'
                            kubectl apply -k .
                            
                            Write-Host 'Checking pod status...'
                            kubectl get pods -n digital-bank
                            
                            Write-Host 'Checking service status...'
                            kubectl get services -n digital-bank
                            
                            Write-Host 'Returning to original directory...'
                            Set-Location $currentDir
                        "
                    '''
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                echo 'Verifying deployment status...'
                script {
                    bat '''
                        powershell -ExecutionPolicy Bypass -Command "
                            Write-Host 'Checking deployment status...'
                            kubectl get deployments -n digital-bank
                            
                            Write-Host 'Checking Istio Gateway...'
                            kubectl get gateway -n digital-bank
                            
                            Write-Host 'Checking Virtual Services...'
                            kubectl get virtualservice -n digital-bank
                            
                            Write-Host 'Checking Ingress...'
                            kubectl get ingress -n digital-bank
                        "
                    '''
                }
            }
        }
        
        stage('Service URLs') {
            steps {
                echo 'Displaying service access information...'
                script {
                    bat '''
                        powershell -ExecutionPolicy Bypass -Command "
                            Write-Host '=== SERVICE ACCESS INFORMATION ==='
                            Write-Host 'Frontend URL: http://digital-bank.example.com'
                            Write-Host ''
                            Write-Host 'To access locally, add this to your hosts file:'
                            Write-Host '127.0.0.1 digital-bank.example.com'
                            Write-Host ''
                            Write-Host 'Current service endpoints:'
                            kubectl get svc -n digital-bank
                        "
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed!'
        }
        success {
            echo '✅ Pipeline succeeded! Services deployed to Kubernetes.'
            echo '🌐 Frontend should be accessible at: http://digital-bank.example.com'
            echo '📝 Add "127.0.0.1 digital-bank.example.com" to your hosts file'
        }
        failure {
            echo '❌ Pipeline failed! Check the logs for details.'
        }
    }
}
