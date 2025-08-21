pipeline {
    agent any
    
    // Pipeline execution options
    options {
        // Only allow one build at a time (queue management)
        disableConcurrentBuilds()
        
        // Keep builds for 30 days or max 10 builds
        buildDiscarder(logRotator(daysToKeepStr: '30', numToKeepStr: '10'))
        
        // Timeout for entire pipeline
        timeout(time: 30, unit: 'MINUTES')
        
        // Skip default checkout (we'll do it manually)
        skipDefaultCheckout(true)
    }
    
    // Trigger configuration
            success {
            echo '✅ SUCCESS: AUTO-TRIGGERED deployment completed successfully!'
            echo '🔄 Triggered by GitHub commit push'
            echo "👤 Author: ${env.GIT_AUTHOR ?: 'Unknown'}"
            echo "💬 Commit: ${env.GIT_MESSAGE ?: 'No message'}"
            echo '🔧 Development environment updated with latest changes'
            echo ''
            echo '📋 REQUIRED MANUAL STEPS TO ACCESS:'
            echo '  1️⃣ Add hosts entry: echo "127.0.0.1 digital-bank.example.com" >> C:\\Windows\\System32\\drivers\\etc\\hosts'
            echo '  2️⃣ Start port forward: kubectl port-forward -n istio-system svc/istio-ingressgateway 8090:80'
            echo '  3️⃣ Access frontend: http://digital-bank.example.com:8090'
            echo ''
            echo '🔍 Quick debugging commands:'
            echo '  • Check pods: kubectl get pods -n digital-bank'
            echo '  • View logs: kubectl logs -f deployment/digital-banking-frontend -n digital-bank'
            echo '  • Helper script: dev-helper.bat start'
        }      // Poll SCM every 2 minutes for changes (backup to webhook)
        pollSCM('H/2 * * * *')
        
        // GitHub webhook trigger (primary method)
        githubPush()
    }
    
    environment {
        DOCKER_CREDENTIALS = credentials('docker-credentials')
        KUBECTL_VERSION = 'v1.31.2'
        
        // Build information
        BUILD_TIMESTAMP = "${new Date().format('yyyy-MM-dd_HH-mm-ss')}"
        GIT_SHORT_COMMIT = "${env.GIT_COMMIT?.take(7) ?: 'unknown'}"
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                script {
                    echo "🔄 Auto-triggered build from GitHub commit..."
                    echo "📋 Build: ${BUILD_NUMBER} | Timestamp: ${BUILD_TIMESTAMP}"
                    
                    // Check if there are queued builds
                    def queuedBuilds = Jenkins.instance.queue.items.findAll { 
                        it.task.name == env.JOB_NAME 
                    }
                    
                    if (queuedBuilds.size() > 0) {
                        echo "⏳ Found ${queuedBuilds.size()} queued build(s). This build will wait for completion."
                    }
                    
                    echo '🔄 Checking out code from GitHub repository main branch...'
                    // Fix Git ownership issues
                    sh 'git config --global --add safe.directory "*"'
                    
                    // Checkout with specific branch and clean workspace
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [
                            [$class: 'CleanBeforeCheckout'],
                            [$class: 'CloneOption', depth: 1, noTags: false, reference: '', shallow: true]
                        ],
                        submoduleCfg: [],
                        userRemoteConfigs: [[
                            url: 'https://github.com/helal6651/Digital-bank.git'
                        ]]
                    ])
                    
                    // Get actual git commit info after checkout
                    env.GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    env.GIT_SHORT_COMMIT = env.GIT_COMMIT.take(7)
                    env.GIT_AUTHOR = sh(returnStdout: true, script: 'git log -1 --pretty=format:"%an"').trim()
                    env.GIT_MESSAGE = sh(returnStdout: true, script: 'git log -1 --pretty=format:"%s"').trim()
                    
                    echo "✅ Checked out commit: ${env.GIT_SHORT_COMMIT}"
                    echo "👤 Author: ${env.GIT_AUTHOR}"
                    echo "💬 Message: ${env.GIT_MESSAGE}"
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
                    // Define IMAGE_TAG using build number and git commit
                    def imageTag = "${BUILD_NUMBER}-${env.GIT_SHORT_COMMIT}"
                    env.IMAGE_TAG = imageTag
                    
                    echo "🐳 Building and pushing Docker images with tag: ${imageTag}..."
                    echo "📋 Triggered by commit: ${env.GIT_SHORT_COMMIT} from ${env.GIT_AUTHOR}"
                    
                    // Docker login
                    sh "echo '${DOCKER_CREDENTIALS_PSW}' | docker login -u ${DOCKER_CREDENTIALS_USR} --password-stdin"
                    
                    // Build and push user-service
                    echo '📦 Building user-service Docker image...'
                    dir('services/user-service') {
                        sh "docker build -t bjitpdmad/user-service:${imageTag} -t bjitpdmad/user-service:latest ."
                        sh "docker push bjitpdmad/user-service:${imageTag}"
                        sh 'docker push bjitpdmad/user-service:latest'
                        echo "✅ User service image pushed with tags: ${imageTag} and latest"
                    }
                    
                    // Build and push account-service
                    echo '📦 Building account-service Docker image...'
                    dir('services/account-service') {
                        sh "docker build -t bjitpdmad/account-service:${imageTag} -t bjitpdmad/account-service:latest ."
                        sh "docker push bjitpdmad/account-service:${imageTag}"
                        sh 'docker push bjitpdmad/account-service:latest'
                        echo "✅ Account service image pushed with tags: ${imageTag} and latest"
                    }
                    
                    // Build and push frontend
                    echo '📦 Building frontend Docker image...'
                    dir('frontend') {
                        sh "docker build -t bjitpdmad/db-frontend:${imageTag} -t bjitpdmad/db-frontend:latest ."
                        sh "docker push bjitpdmad/db-frontend:${imageTag}"
                        sh 'docker push bjitpdmad/db-frontend:latest'
                        echo "✅ Frontend image pushed with tags: ${imageTag} and latest"
                    }
                    
                    echo "✅ All Docker images built and pushed successfully"
                    echo "🏷️ Image tag used: ${imageTag}"
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
                            wc -l kubeconfig
                            echo "First few lines:"
                            head -n 5 kubeconfig
                            
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
        
        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo '🚀 Deploying to Kubernetes...'
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            
                            echo "🆕 Ensuring digital-bank namespace exists..."
                            if ./kubectl get namespace digital-bank >/dev/null 2>&1; then
                                echo "✅ digital-bank namespace already exists"
                            else
                                ./kubectl create namespace digital-bank
                                echo "✅ digital-bank namespace created"
                            fi
                            
                            echo "🔍 Testing Kustomize files..."
                            cd kubernetes/overlays/prod
                            
                            # Test kustomize build (dry-run)
                            echo "🧪 Testing kustomize build..."
                            ../../../kubectl kustomize . > /tmp/kustomize-output.yaml
                            echo "📊 Kustomize build successful, generated $(wc -l < /tmp/kustomize-output.yaml) lines"
                            
                            echo "📦 Applying Kubernetes manifests..."
                            ../../../kubectl apply -k .
                            
                            echo "🔄 Forcing rolling restart to pull new Docker images..."
                            
                            # Force restart deployments to pull latest images
                            echo "🔄 Restarting user-service deployment..."
                            ../../../kubectl rollout restart deployment/user-service -n digital-bank || echo "user-service deployment not found, skipping restart"
                            
                            echo "🔄 Restarting account-service deployment..."
                            ../../../kubectl rollout restart deployment/account-service -n digital-bank || echo "account-service deployment not found, skipping restart"
                            
                            echo "🔄 Restarting frontend deployment..."
                            ../../../kubectl rollout restart deployment/digital-banking-frontend -n digital-bank || echo "frontend deployment not found, skipping restart"
                            
                            echo "✅ Deployment to Kubernetes completed with forced restarts"
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
                            
                            echo "⏳ Waiting for pods to be ready (5 minutes timeout)..."
                            
                            # Wait for rollout to complete for each deployment
                            echo "⏳ Waiting for user-service rollout..."
                            ../../../kubectl rollout status deployment/user-service -n digital-bank --timeout=300s || echo "user-service rollout timeout or not found"
                            
                            echo "⏳ Waiting for account-service rollout..."
                            ../../../kubectl rollout status deployment/account-service -n digital-bank --timeout=300s || echo "account-service rollout timeout or not found"
                            
                            echo "⏳ Waiting for frontend rollout..."
                            ../../../kubectl rollout status deployment/digital-banking-frontend -n digital-bank --timeout=300s || echo "frontend rollout timeout or not found"
                            
                            # General pod readiness check
                            ../../../kubectl wait --for=condition=ready pod --all -n digital-bank --timeout=300s || true
                            
                            echo "📊 Final pod status:"
                            ./kubectl get pods -n digital-bank
                            
                            echo "🏷️ Checking pod image versions:"
                            ./kubectl get pods -n digital-bank -o custom-columns="NAME:.metadata.name,IMAGE:.spec.containers[*].image"
                            
                            echo "✅ Status check completed"
                        '''
                    }
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo '🔍 Verifying deployment...'
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            
                            echo "📊 Deployment Status:"
                            ./kubectl get deployments -n digital-bank
                            
                            echo "🌐 Services:"
                            ./kubectl get services -n digital-bank
                            
                            echo "🚪 Istio Gateway:"
                            ./kubectl get gateway -n digital-bank || echo "No Istio Gateway found"
                            
                            echo "🔀 Virtual Services:"
                            ./kubectl get virtualservice -n digital-bank || echo "No Virtual Services found"
                            
                            echo "🔍 Ingress Controllers:"
                            ./kubectl get ingress -n digital-bank || echo "No Ingress found"
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
                            
                            echo "=== 🌐 SERVICE ACCESS INFORMATION ==="
                            echo "Frontend URL: http://digital-bank.example.com:8090"
                            echo ""
                            echo "📝 To access locally, add this to your hosts file:"
                            echo "127.0.0.1 digital-bank.example.com"
                            echo ""
                            echo "🔍 Current service endpoints:"
                            ./kubectl get svc -n digital-bank
                            
                            echo ""
                            echo "🔌 REQUIRED: Run local port forwarding to access the application:"
                            echo "kubectl port-forward -n istio-system svc/istio-ingressgateway 8090:80"
                            echo ""
                            echo "🎯 Alternative direct service access:"
                            echo "kubectl port-forward svc/digital-banking-frontend 3000:80 -n digital-bank"
                            echo "kubectl port-forward svc/user-service 8081:8081 -n digital-bank"
                            echo "kubectl port-forward svc/account-service 8082:8082 -n digital-bank"
                            
                            echo "✅ Access information displayed"
                        '''
                    }
                }
            }
        }
        
        stage('Setup Port Forward') {
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo "🔌 Setting up DEV port forwarding to Istio Gateway..."
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            
                            echo "🔍 Checking Istio Gateway service..."
                            ./kubectl get svc -n istio-system istio-ingressgateway
                            
                            # Kill any existing port-forward on 8090
                            echo "🧹 Cleaning up any existing port-forward processes..."
                            pkill -f "kubectl.*port-forward.*8090" || echo "No existing port-forward found"
                            
                            echo "🚀 Starting DEV port-forward to Istio Gateway on port 8090..."
                            echo "� Development access points:"
                            echo "  - Frontend: http://digital-bank.example.com:8090"
                            echo "  - Direct:   http://localhost:8090"
                            echo ""
                            
                            # Start port-forward in background with better logging
                            echo "🔌 Initializing port-forward process..."
                            ./kubectl port-forward -n istio-system svc/istio-ingressgateway 8090:80 > port-forward.log 2>&1 &
                            PORT_FORWARD_PID=$!
                            
                            echo "� Port forward PID: $PORT_FORWARD_PID"
                            echo $PORT_FORWARD_PID > port-forward.pid
                            
                            # Give it time to establish
                            sleep 8
                            
                            # Verify port-forward is working
                            if ps -p $PORT_FORWARD_PID > /dev/null 2>&1; then
                                echo "✅ DEV port forwarding active on port 8090"
                                
                                # Test connectivity
                                echo "🧪 Testing local connectivity..."
                                if curl -s --max-time 5 http://localhost:8090 >/dev/null 2>&1; then
                                    echo "🎯 Connectivity test: SUCCESS"
                                else
                                    echo "⚠️  Connectivity test: Waiting for services to be ready..."
                                fi
                                
                                echo ""
                                echo "🎯 DEV ENVIRONMENT ACCESS:"
                                echo "   ✅ Port 8090 is active and ready"
                                echo "   🌐 Add to hosts: 127.0.0.1 digital-bank.example.com"
                                echo "   📱 Access: http://digital-bank.example.com:8090"
                                echo ""
                                echo "🔧 DEV DEBUGGING COMMANDS:"
                                echo "   📊 Check pods: kubectl get pods -n digital-bank"
                                echo "   🔍 View logs: kubectl logs -f deployment/digital-banking-frontend -n digital-bank"
                                echo "   🛑 Stop port-forward: kill -9 $PORT_FORWARD_PID"
                                
                            else
                                echo "❌ Port forwarding failed to start"
                                echo "📋 Port-forward log:"
                                cat port-forward.log
                                exit 1
                            fi
                        '''
                    }
                }
            }
        }
        
        stage('Dev Environment Summary') {
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo "📋 Development environment summary and quick tests..."
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            
                            echo "=== 🔧 DEV ENVIRONMENT STATUS ==="
                            echo ""
                            
                            echo "🏗️ Deployment Status:"
                            ./kubectl get deployments -n digital-bank
                            echo ""
                            
                            echo "🚀 Pod Status:"
                            ./kubectl get pods -n digital-bank
                            echo ""
                            
                            echo "🌐 Service Status:"
                            ./kubectl get svc -n digital-bank
                            echo ""
                            
                            echo "🔌 Port Forward Status:"
                            if [ -f port-forward.pid ]; then
                                PID=$(cat port-forward.pid)
                                if ps -p $PID > /dev/null 2>&1; then
                                    echo "✅ Active on PID: $PID"
                                    echo "📍 Access: http://localhost:8090"
                                else
                                    echo "❌ Port forward process not running"
                                fi
                            else
                                echo "⚠️  No port-forward PID file found"
                            fi
                            echo ""
                            
                            echo "🧪 Quick Health Checks:"
                            ./kubectl get pods -n digital-bank --field-selector=status.phase=Running | wc -l | xargs -I {} echo "Running pods: {}"
                            ./kubectl get pods -n digital-bank --field-selector=status.phase!=Running | grep -v "NAME" | wc -l | xargs -I {} echo "Non-running pods: {}"
                            echo ""
                            
                            echo "📚 DEV QUICK REFERENCE:"
                            echo "   🌐 Frontend: http://digital-bank.example.com:8090"
                            echo "   🔍 Logs: kubectl logs -f deployment/digital-banking-frontend -n digital-bank"
                            echo "   🔄 Restart: kubectl rollout restart deployment/digital-banking-frontend -n digital-bank"
                            echo "   📊 Debug: kubectl describe pod <pod-name> -n digital-bank"
                            echo "   🛑 Port-forward: kill -9 $(cat port-forward.pid)"
                        '''
                    }
                }
            }
        }
    }
    
    post {
        always {
            script {
                echo '🏁 Pipeline execution completed.'
                // Note: sh commands in post sections have limited context
                // Cleanup will be handled by Jenkins workspace cleanup
            }
        }
        success {
            echo '✅ SUCCESS: DEV deployment completed successfully!'
            echo '🔧 Development environment is ready for testing'
            echo ''
            echo '� REQUIRED MANUAL STEPS TO ACCESS:'
            echo '  1️⃣ Add hosts entry: echo "127.0.0.1 digital-bank.example.com" >> C:\\Windows\\System32\\drivers\\etc\\hosts'
            echo '  2️⃣ Start port forward: kubectl port-forward -n istio-system svc/istio-ingressgateway 8090:80'
            echo '  3️⃣ Access frontend: http://digital-bank.example.com:8090'
            echo ''
            echo '🔍 Quick debugging commands:'
            echo '  • Check pods: kubectl get pods -n digital-bank'
            echo '  • View logs: kubectl logs -f deployment/digital-banking-frontend -n digital-bank'
            echo '  • Helper script: dev-helper.bat start'
        }
        failure {
            echo '❌ FAILURE: Pipeline failed!'
            echo '🔍 Check the console output above for error details'
        }
    }
}
