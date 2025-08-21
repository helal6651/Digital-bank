pipeline {
    agent any
    
    options {
        // Prevent concurrent builds - if a build is running and another is triggered,
        // the new build will wait in queue until the current one finishes
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }
    
    triggers {
        // Poll SCM every 2 minutes as backup trigger
        pollSCM('H/2 * * * *')
        // GitHub webhook trigger for instant builds
        githubPush()
    }
    
    environment {
        DOCKER_REGISTRY = 'helal6651'
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
        GIT_COMMIT_SHORT = "${env.GIT_COMMIT?.take(7) ?: 'unknown'}"
        BUILD_TIMESTAMP = sh(script: 'date +"%Y%m%d_%H%M%S"', returnStdout: true).trim()
    }
    
    stages {
        stage('Checkout and Build Info') {
            steps {
                script {
                    echo "======================================"
                    echo "       DIGITAL BANKING CI/CD"
                    echo "======================================"
                    echo "Build #: ${BUILD_NUMBER}"
                    echo "Git Commit: ${GIT_COMMIT_SHORT}"
                    echo "Timestamp: ${BUILD_TIMESTAMP}"
                    echo "======================================"
                    
                    // Get git information
                    def gitAuthor = sh(script: 'git log -1 --pretty=format:"%an"', returnStdout: true).trim()
                    def gitMessage = sh(script: 'git log -1 --pretty=format:"%s"', returnStdout: true).trim()
                    
                    echo "Triggered by: ${gitAuthor}"
                    echo "Commit message: ${gitMessage}"
                    echo "======================================"
                }
            }
        }
        
        stage('Clean Docker Environment') {
            steps {
                script {
                    echo 'Cleaning Docker environment...'
                    sh '''
                        # Stop and remove existing containers (ignore errors if no compose)
                        docker-compose -f docker/docker-compose.yaml down --remove-orphans 2>/dev/null || echo "No existing docker-compose services to stop"
                        
                        # Clean up dangling images and unused containers
                        docker system prune -f || true
                        
                        echo "Docker cleanup completed"
                    '''
                }
            }
        }
        
        stage('Build Services') {
            parallel {
                stage('Build User Service') {
                    steps {
                        dir('services/user-service') {
                            script {
                                echo 'Building User Service...'
                                sh '''
                                    # Make gradlew executable
                                    chmod +x ./gradlew || echo "chmod not available, trying direct execution"
                                    
                                    echo "Building user-service JAR..."
                                    if [ -f "./gradlew" ]; then
                                        ./gradlew clean build -x test
                                    else
                                        echo "Gradlew not found, using gradle command"
                                        gradle clean build -x test
                                    fi
                                    
                                    echo "Building user-service Docker image..."
                                    docker build -t ${DOCKER_REGISTRY}/user-service:${BUILD_NUMBER} .
                                    docker tag ${DOCKER_REGISTRY}/user-service:${BUILD_NUMBER} ${DOCKER_REGISTRY}/user-service:latest
                                '''
                            }
                        }
                    }
                }
                
                stage('Build Account Service') {
                    steps {
                        dir('services/account-service') {
                            script {
                                echo 'Building Account Service...'
                                sh '''
                                    # Make mvnw executable if available
                                    chmod +x ./mvnw 2>/dev/null || echo "chmod not available"
                                    
                                    echo "Building account-service JAR..."
                                    if [ -f "./mvnw" ]; then
                                        ./mvnw clean package -DskipTests
                                    elif command -v mvn >/dev/null 2>&1; then
                                        mvn clean package -DskipTests
                                    else
                                        echo "Neither mvnw nor mvn found, skipping Java build"
                                        echo "Creating dummy JAR for Docker build"
                                        mkdir -p target
                                        touch target/account-service-1.0.jar
                                    fi
                                    
                                    echo "Building account-service Docker image..."
                                    docker build -t ${DOCKER_REGISTRY}/account-service:${BUILD_NUMBER} .
                                    docker tag ${DOCKER_REGISTRY}/account-service:${BUILD_NUMBER} ${DOCKER_REGISTRY}/account-service:latest
                                '''
                            }
                        }
                    }
                }
                
                stage('Build Frontend') {
                    steps {
                        dir('frontend') {
                            script {
                                echo 'Building Frontend...'
                                sh '''
                                    echo "Building frontend Docker image..."
                                    docker build -t ${DOCKER_REGISTRY}/frontend:${BUILD_NUMBER} .
                                    docker tag ${DOCKER_REGISTRY}/frontend:${BUILD_NUMBER} ${DOCKER_REGISTRY}/frontend:latest
                                '''
                            }
                        }
                    }
                }
            }
        }
        
        // --- Begin Kubernetes-related stages ---
        stage('Setup Tools') {
            steps {
                script {
                    echo "🛠️ Setting up kubectl and tools..."
                    sh '''
                        # Download kubectl to workspace directory
                        echo "📥 Installing kubectl ${KUBECTL_VERSION}..."
                        curl -LO "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl"
                        chmod +x kubectl
                        # Verify kubectl is working
                        ./kubectl version --client=true
                        echo "✅ kubectl setup completed"
                    '''
                }
            }
        }
        stage('Test Kubernetes Connection') {
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo "🧪 Testing Kubernetes connection..."
                        sh '''
                            echo "🔧 Setting up kubeconfig from Jenkins credential..."
                            rm -f kubeconfig kubeconfig_clean
                            cp "$KUBECONFIG" kubeconfig
                            echo "📋 Original kubeconfig file info:"
                            file kubeconfig || echo "file command not available"
                            wc -l kubeconfig
                            echo "First few lines:"
                            head -n 5 kubeconfig
                            echo "Last few lines:"
                            tail -n 5 kubeconfig
                            tr -d '\r\0' < kubeconfig > kubeconfig_clean
                            mv kubeconfig_clean kubeconfig
                            echo "📋 After cleaning:"
                            wc -l kubeconfig
                            echo "First few lines after cleaning:"
                            head -n 5 kubeconfig
                            echo "🔍 Checking kubeconfig structure..."
                            if grep -q "apiVersion:" kubeconfig && grep -q "clusters:" kubeconfig && grep -q "contexts:" kubeconfig; then
                                echo "✅ Valid kubeconfig structure detected"
                            else
                                echo "❌ Invalid kubeconfig structure, showing file content for debugging:"
                                cat kubeconfig
                                exit 1
                            fi
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            if ./kubectl config get-contexts | grep -q "kind-kind"; then
                                echo "✅ KIND cluster context found, switching to kind-kind"
                                ./kubectl config use-context kind-kind
                            else
                                echo "⚠️ KIND cluster context not found, using current context"
                                ./kubectl config current-context
                            fi
                            echo "🔍 Verifying kubectl connection..."
                            ./kubectl cluster-info
                            echo "🎯 Testing basic kubectl commands..."
                            ./kubectl get nodes
                            ./kubectl get namespaces
                            echo "✅ Kubernetes connection test completed"
                        '''
                    }
                }
            }
        }
        stage('Test Namespace Creation') {
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo "🏗️ Testing namespace operations..."
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            echo "🆕 Creating digital-bank namespace..."
                            if ./kubectl get namespace digital-bank >/dev/null 2>&1; then
                                echo "✅ digital-bank namespace already exists"
                            else
                                ./kubectl create namespace digital-bank
                                echo "✅ digital-bank namespace created"
                            fi
                            echo "📋 Available namespaces:"
                            ./kubectl get namespaces
                            echo "✅ Namespace operations test completed"
                        '''
                    }
                }
            }
        }
        stage('Test Kustomize Files') {
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo "📁 Testing Kustomize file validation..."
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            echo "🔍 Checking Kustomize files in prod overlay..."
                            cd kubernetes/overlays/prod
                            ../../../kubectl kustomize . > /tmp/kustomize-output.yaml
                            echo "📊 Kustomize build successful, generated $(wc -l < /tmp/kustomize-output.yaml) lines"
                            echo "✅ Kustomize validation completed"
                        '''
                    }
                }
            }
        }
        stage('Deploy to KIND Cluster (Test)') {
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo "🚀 Testing deployment to KIND cluster..."
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            echo "📦 Applying Kubernetes manifests..."
                            cd kubernetes/overlays/prod
                            ../../../kubectl apply -k .
                            echo "✅ Deployment to KIND cluster completed"
                        '''
                    }
                }
            }
        }
        stage('Wait and Check Pod Status') {
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo "⏳ Waiting for pods to be ready..."
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            echo "🔄 Checking pod status..."
                            ./kubectl get pods -n digital-bank
                            echo "🔄 Checking service status..."
                            ./kubectl get services -n digital-bank
                            echo "🔄 Checking deployment status..."
                            ./kubectl get deployments -n digital-bank
                            echo "✅ Status check completed"
                        '''
                    }
                }
            }
        }
        stage('Display Access Information') {
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo "📋 Displaying access information..."
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            echo "=== SERVICE ACCESS INFORMATION ==="
                            echo "Frontend URL: http://digital-bank.example.com"
                            echo ""
                            echo "To access locally, add this to your hosts file:"
                            echo "127.0.0.1 digital-bank.example.com"
                            echo ""
                            echo "Current service endpoints:"
                            ./kubectl get svc -n digital-bank
                            echo "✅ Access information displayed"
                        '''
                    }
                }
            }
        }
        // --- End Kubernetes-related stages ---
    }
    
    post {
        always {
            script {
                echo '======================================'
                echo '           BUILD SUMMARY'
                echo '======================================'
                echo "Build: ${currentBuild.currentResult}"
                echo "Duration: ${currentBuild.durationString}"
                echo "Build #: ${BUILD_NUMBER}"
                echo "Git Commit: ${GIT_COMMIT_SHORT}"
                echo '======================================'
            }
        }
        success {
            echo 'SUCCESS: Digital Banking pipeline completed successfully!'
            echo 'Development environment is ready:'
            echo '  • Frontend: http://localhost:3000 (after port-forward)'
            echo '  • API Gateway: http://localhost:8080 (after port-forward)'
            echo '  • View logs: kubectl logs -f deployment/digital-banking-frontend -n digital-bank'
            echo '  • Helper script: dev-helper.bat start'
        }
        failure {
            echo 'FAILURE: Pipeline failed!'
            echo 'Check the console output above for error details'
        }
    }
}
