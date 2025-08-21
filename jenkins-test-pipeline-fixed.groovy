pipeline {
    agent any
    
    environment {
        KUBECTL_VERSION = 'v1.31.2'
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                script {
                    echo "🔄 Checking out code from GitHub repository main branch..."
                    sh 'git config --global --add safe.directory "*"'
                    git branch: 'main', url: 'https://github.com/helal6651/Digital-bank.git'
                }
            }
        }
        
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
                            
                            # Clean any previous files
                            rm -f kubeconfig kubeconfig_clean
                            
                            # Copy and clean the kubeconfig file
                            cp "$KUBECONFIG" kubeconfig
                            
                            echo "📋 Original kubeconfig file info:"
                            file kubeconfig || echo "file command not available"
                            wc -l kubeconfig
                            echo "First few lines:"
                            head -n 5 kubeconfig
                            echo "Last few lines:"
                            tail -n 5 kubeconfig
                            
                            # Clean Windows line endings and null characters
                            tr -d '\\r\\0' < kubeconfig > kubeconfig_clean
                            mv kubeconfig_clean kubeconfig
                            
                            echo "📋 After cleaning:"
                            wc -l kubeconfig
                            echo "First few lines after cleaning:"
                            head -n 5 kubeconfig
                            
                            # Check if kubeconfig has basic structure instead of YAML validation
                            echo "🔍 Checking kubeconfig structure..."
                            if grep -q "apiVersion:" kubeconfig && grep -q "clusters:" kubeconfig && grep -q "contexts:" kubeconfig; then
                                echo "✅ Valid kubeconfig structure detected"
                            else
                                echo "❌ Invalid kubeconfig structure, showing file content for debugging:"
                                cat kubeconfig
                                exit 1
                            fi
                            
                            # Export kubeconfig
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            
                            # Check if KIND context exists
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
                            
                            # Test kustomize build (dry-run)
                            echo "🧪 Testing kustomize build..."
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
    }
    
    post {
        always {
            script {
                echo "🏁 Test pipeline execution completed."
                sh 'rm -f kubeconfig kubeconfig_clean kubectl'
            }
        }
        success {
            echo "✅ SUCCESS: Kubernetes deployment test completed successfully!"
            echo "🎉 Your KIND cluster is working properly"
            echo "▶️ You can now run the full pipeline with confidence"
        }
        failure {
            echo "❌ FAILURE: Kubernetes deployment test failed!"
            echo "🔍 Check the console output above for error details"
            echo "🛠️ Fix the issues before running the full pipeline"
        }
    }
}
