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
                                // Ensure Dockerfile exists with correct content
                                writeFile file: 'user-service/Dockerfile', text: '''FROM openjdk:17-jdk-slim
COPY build/libs/user-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]'''
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
                                // Ensure Dockerfile exists with correct content
                                writeFile file: 'Dockerfile', text: '''FROM openjdk:17-jdk-slim
COPY target/account-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app.jar"]'''
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
                    
                    sh '''#!/bin/bash
                        # Install kubectl if not present (without sudo)
                        if ! command -v kubectl &> /dev/null; then
                            echo "📥 Installing kubectl..."
                            curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
                            chmod +x kubectl
                            mkdir -p $HOME/bin
                            mv kubectl $HOME/bin/
                            export PATH=$HOME/bin:$PATH
                        fi
                        
                        # Use Jenkins kubeconfig credential for existing KIND cluster
                        echo "🔧 Setting up kubeconfig from Jenkins credential..."
                        
                        # Jenkins credential should be a file, copy it directly
                        if [ -f "$KUBECONFIG" ]; then
                            echo "✅ Jenkins kubeconfig file found: $KUBECONFIG"
                            cp "$KUBECONFIG" kubeconfig
                            echo "📄 Copied kubeconfig file, size: $(wc -c < kubeconfig) bytes"
                            
                            # Verify the kubeconfig
                            echo "🔍 Verifying kubeconfig format..."
                            if grep -q "apiVersion:" kubeconfig && grep -q "kind:" kubeconfig; then
                                echo "✅ Valid YAML kubeconfig detected"
                                
                                # Check if KIND cluster context exists
                                if grep -q "kind-kind" kubeconfig; then
                                    echo "✅ KIND cluster context found in kubeconfig"
                                    export KUBECONFIG=${PWD}/kubeconfig
                                    kubectl config use-context kind-kind
                                    echo "✅ Successfully switched to kind-kind context"
                                    KUBECONFIG_VALID=true
                                else
                                    echo "⚠️ No KIND cluster context found, checking available contexts..."
                                    export KUBECONFIG=${PWD}/kubeconfig
                                    kubectl config get-contexts
                                    KUBECONFIG_VALID=false
                                fi
                            else
                                echo "❌ Invalid kubeconfig format from Jenkins credential"
                                echo "� First 300 characters of kubeconfig:"
                                head -c 300 kubeconfig
                                echo ""
                                KUBECONFIG_VALID=false
                            fi
                        else
                            echo "❌ Jenkins kubeconfig credential is not a file: $KUBECONFIG"
                            echo "📋 Please ensure kubectl-config is uploaded as 'Secret file' type in Jenkins"
                            KUBECONFIG_VALID=false
                        fi
                        
                        # Fallback: Try to get kubeconfig from existing KIND cluster
                        if [ "$KUBECONFIG_VALID" != "true" ]; then
                            echo "🔧 Fallback: Getting kubeconfig from existing KIND cluster..."
                            
                            # Install KIND if not available
                            if ! command -v kind &> /dev/null; then
                                echo "📥 Installing KIND..."
                                curl -Lo kind "https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64"
                                chmod +x kind
                                mkdir -p $HOME/bin
                                mv kind $HOME/bin/
                                export PATH=$HOME/bin:$PATH
                            fi
                            
                            # List available KIND clusters
                            echo "📋 Checking for existing KIND clusters..."
                            if CLUSTERS=$(kind get clusters 2>/dev/null) && [ -n "$CLUSTERS" ]; then
                                echo "Available clusters: $CLUSTERS"
                                
                                # Try to find your cluster (try common names)
                                for cluster_name in "kind" "digital-bank" "default"; do
                                    if echo "$CLUSTERS" | grep -q "^${cluster_name}$"; then
                                        echo "✅ Found KIND cluster: $cluster_name"
                                        kind get kubeconfig --name="$cluster_name" > kubeconfig
                                        export KUBECONFIG=${PWD}/kubeconfig
                                        echo "✅ Using KIND cluster: $cluster_name"
                                        break
                                    fi
                                done
                                
                                # If no specific cluster found, use the first available
                                if ! kubectl cluster-info &>/dev/null; then
                                    FIRST_CLUSTER=$(echo "$CLUSTERS" | head -n1)
                                    echo "✅ Using first available cluster: $FIRST_CLUSTER"
                                    kind get kubeconfig --name="$FIRST_CLUSTER" > kubeconfig
                                    export KUBECONFIG=${PWD}/kubeconfig
                                fi
                            else
                                echo "❌ No KIND clusters found"
                                echo "📋 Please ensure your KIND cluster is running in Docker Desktop"
                                exit 1
                            fi
                        
                        # Set the kubeconfig environment variable
                        export KUBECONFIG=${PWD}/kubeconfig
                        
                        # Set context to KIND cluster if available
                        if kubectl config get-contexts -o name | grep -q "kind-kind"; then
                            echo "🎯 Setting context to kind-kind..."
                            kubectl config use-context kind-kind
                        else
                            echo "📋 Available contexts:"
                            kubectl config get-contexts
                            echo "🎯 Using current context..."
                        fi
                        
                        # Verify kubectl connection
                        echo "🔍 Verifying kubectl connection..."
                        kubectl cluster-info
                        
                        # Navigate to prod overlay
                        cd kubernetes/overlays/prod
                        
                        # Apply the configurations
                        echo "🚀 Applying Kubernetes configurations..."
                        kubectl apply -k .
                        
                        # Wait for pods to be ready
                        echo "⏳ Waiting for pods to be ready..."
                        kubectl wait --for=condition=ready pod --all -n digital-bank --timeout=300s || true
                        
                        # Show deployment status
                        echo "📊 Deployment Status:"
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
                        # Ensure kubectl is available
                        if ! command -v kubectl &> /dev/null; then
                            echo "📥 Installing kubectl..."
                            curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
                            chmod +x kubectl
                            mkdir -p $HOME/bin
                            mv kubectl $HOME/bin/
                            export PATH=$HOME/bin:$PATH
                        fi
                        
                        # Use the kubeconfig created in deployment stage
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
                        # Ensure kubectl is available
                        if ! command -v kubectl &> /dev/null; then
                            echo "📥 Installing kubectl..."
                            curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
                            chmod +x kubectl
                            mkdir -p $HOME/bin
                            mv kubectl $HOME/bin/
                            export PATH=$HOME/bin:$PATH
                        fi
                        
                        # Use the kubeconfig created in deployment stage
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
