pipeline {
    agent any
    
    environment {
        DOCKER_CREDENTIALS = credentials('docker-credentials')
        KUBECONFIG = credentials('kubectl-config')
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                script {
                    echo '🔄 Checking out code from GitHub repository main branch...'
                    // Fix Git ownership issues
                    sh 'git config --global --add safe.directory "*"'
                    git branch: 'main', url: 'https://github.com/helal6651/Digital-bank.git'
                }
            }
        }
        
        stage('Build Common Service (Dependency)') {
            steps {
                script {
                    echo '🏗️ Building common-service as dependency...'
                    dir('services') {
                        sh 'chmod +x ./gradlew'
                        sh './gradlew :common-service:clean'
                        sh './gradlew :common-service:build -x test'
                        sh './gradlew :common-service:publishToMavenLocal'
                        echo '✅ Common service built and published to local Maven repository'
                    }
                }
            }
        }
        
        stage('Build Services') {
            parallel {
                stage('Build User Service') {
                    steps {
                        script {
                            echo '🏗️ Building user-service...'
                            dir('services') {
                                sh './gradlew :user-service:clean'
                                sh './gradlew :user-service:build -x test'
                                echo '✅ User service built successfully'
                            }
                        }
                    }
                }
                
                stage('Build Account Service') {
                    steps {
                        script {
                            echo '🏗️ Building account-service...'
                            dir('services/account-service') {
                                sh 'chmod +x ./mvnw'
                                sh './mvnw clean package -DskipTests'
                                echo '✅ Account service built successfully'
                            }
                        }
                    }
                }
                
                stage('Build Frontend') {
                    steps {
                        script {
                            echo '🏗️ Building frontend...'
                            dir('frontend') {
                                sh 'npm install'
                                sh 'npm run build'
                                echo '✅ Frontend built successfully'
                            }
                        }
                    }
                }
            }
        }
        
        stage('Build and Push Docker Images') {
            steps {
                script {
                    echo '🐳 Building and pushing Docker images...'
                    
                    // Docker login
                    sh "echo '${DOCKER_CREDENTIALS_PSW}' | docker login -u ${DOCKER_CREDENTIALS_USR} --password-stdin"
                    
                    // Build and push user-service
                    echo '📦 Building user-service Docker image...'
                    dir('services/user-service') {
                        sh 'docker build -t bjitpdmad/user-service:latest .'
                        sh 'docker push bjitpdmad/user-service:latest'
                    }
                    
                    // Build and push account-service
                    echo '📦 Building account-service Docker image...'
                    dir('services/account-service') {
                        sh 'docker build -t bjitpdmad/account-service:latest .'
                        sh 'docker push bjitpdmad/account-service:latest'
                    }
                    
                    // Build and push frontend
                    echo '📦 Building frontend Docker image...'
                    dir('frontend') {
                        sh 'docker build -t bjitpdmad/db-frontend:latest .'
                        sh 'docker push bjitpdmad/db-frontend:latest'
                    }
                    
                    sh 'docker logout'
                    echo '🧹 Docker logout completed'
                }
            }
        }
        
        stage('Update Kustomize Image Tags') {
            steps {
                script {
                    echo '🏷️ Updating Kustomize image tags...'
                    dir('kubernetes/overlays/prod') {
                        sh '''
                            sed -i 's|newTag: .*|newTag: latest|g' kustomization.yaml
                        '''
                        echo '✅ Kustomize image tags updated'
                    }
                }
            }
        }
        
        stage('Deploy to KIND Cluster') {
            steps {
                script {
                    echo '🚀 Deploying to KIND cluster...'
                    
                    // Save kubeconfig to a file
                    writeFile file: 'kubeconfig', encoding: 'Base64', text: env.KUBECONFIG
                    
                    sh '''
                        export KUBECONFIG=${PWD}/kubeconfig
                        
                        # Navigate to prod overlay
                        cd kubernetes/overlays/prod
                        
                        # Apply the configurations
                        kubectl apply -k .
                        
                        # Wait for pods to be ready
                        echo "⏳ Waiting for pods to be ready..."
                        kubectl wait --for=condition=ready pod --all -n digital-bank --timeout=300s || true
                        
                        # Show deployment status
                        kubectl get pods -n digital-bank
                        kubectl get services -n digital-bank
                    '''
                    
                    echo '✅ Deployment completed'
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                script {
                    echo '🔍 Verifying deployment...'
                    sh '''
                        export KUBECONFIG=${PWD}/kubeconfig
                        
                        echo "📊 Deployment Status:"
                        kubectl get deployments -n digital-bank
                        
                        echo "🌐 Services:"
                        kubectl get services -n digital-bank
                        
                        echo "🚪 Istio Gateway:"
                        kubectl get gateway -n digital-bank || echo "No Istio Gateway found"
                        
                        echo "🔀 Virtual Services:"
                        kubectl get virtualservice -n digital-bank || echo "No Virtual Services found"
                    '''
                }
            }
        }
        
        stage('Display Access Information') {
            steps {
                script {
                    echo '📋 Service Access Information'
                    sh '''
                        export KUBECONFIG=${PWD}/kubeconfig
                        
                        echo "=== 🌐 SERVICE ACCESS INFORMATION ==="
                        echo "Frontend URL: http://digital-bank.example.com"
                        echo ""
                        echo "📝 To access locally, add this to your hosts file:"
                        echo "127.0.0.1 digital-bank.example.com"
                        echo ""
                        echo "🔍 Current service endpoints:"
                        kubectl get svc -n digital-bank
                        
                        echo ""
                        echo "🎯 Port forwards for local testing:"
                        echo "kubectl port-forward svc/front-end-service 3000:80 -n digital-bank"
                        echo "kubectl port-forward svc/user-service 8081:8081 -n digital-bank"
                        echo "kubectl port-forward svc/account-service 8082:8082 -n digital-bank"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo '🏁 Pipeline execution completed.'
        }
        success {
            echo '✅ SUCCESS: All services deployed successfully!'
            echo '🌐 Frontend accessible at: http://digital-bank.example.com'
            echo '📝 Remember to add "127.0.0.1 digital-bank.example.com" to your hosts file'
        }
        failure {
            echo '❌ FAILURE: Pipeline failed!'
            echo '🔍 Check the console output above for error details'
        }
        cleanup {
            // Clean up kubeconfig file
            sh 'rm -f kubeconfig || true'
        }
    }
}
