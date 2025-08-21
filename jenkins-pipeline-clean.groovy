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
        // GitHub webhook trigger (requires GitHub plugin)
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
                        # Stop and remove existing containers
                        docker-compose -f docker/docker-compose.yaml down --remove-orphans || true
                        
                        # Clean up dangling images and unused containers
                        docker system prune -f || true
                        
                        # Remove old images of our services (keep last 3 versions)
                        for service in user-service account-service notification-service api-gateway frontend; do
                            echo "Cleaning old images for $service..."
                            docker images ${DOCKER_REGISTRY}/$service --format "table {{.Repository}}:{{.Tag}}\\t{{.CreatedAt}}" | grep -v REPOSITORY | tail -n +4 | awk '{print $1}' | xargs -r docker rmi || true
                        done
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
                                    echo "Building user-service JAR..."
                                    ./gradlew clean build -x test
                                    
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
                                    echo "Building account-service JAR..."
                                    mvn clean package -DskipTests
                                    
                                    echo "Building account-service Docker image..."
                                    docker build -t ${DOCKER_REGISTRY}/account-service:${BUILD_NUMBER} .
                                    docker tag ${DOCKER_REGISTRY}/account-service:${BUILD_NUMBER} ${DOCKER_REGISTRY}/account-service:latest
                                '''
                            }
                        }
                    }
                }
                
                stage('Build Notification Service') {
                    steps {
                        dir('services/notification-service') {
                            script {
                                echo 'Building Notification Service...'
                                sh '''
                                    echo "Building notification-service JAR..."
                                    ./gradlew clean build -x test
                                    
                                    echo "Building notification-service Docker image..."
                                    docker build -t ${DOCKER_REGISTRY}/notification-service:${BUILD_NUMBER} .
                                    docker tag ${DOCKER_REGISTRY}/notification-service:${BUILD_NUMBER} ${DOCKER_REGISTRY}/notification-service:latest
                                '''
                            }
                        }
                    }
                }
                
                stage('Build API Gateway') {
                    steps {
                        dir('services/api-gateway') {
                            script {
                                echo 'Building API Gateway...'
                                sh '''
                                    echo "Building api-gateway JAR..."
                                    mvn clean package -DskipTests
                                    
                                    echo "Building api-gateway Docker image..."
                                    docker build -t ${DOCKER_REGISTRY}/api-gateway:${BUILD_NUMBER} .
                                    docker tag ${DOCKER_REGISTRY}/api-gateway:${BUILD_NUMBER} ${DOCKER_REGISTRY}/api-gateway:latest
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
        
        stage('Deploy to Development') {
            steps {
                script {
                    echo 'Deploying to development environment...'
                    sh '''
                        # Update docker-compose with new build number
                        export DOCKER_TAG=${BUILD_NUMBER}
                        
                        echo "Starting services with docker-compose..."
                        cd docker
                        docker-compose up -d
                        
                        echo "Waiting for services to be ready..."
                        sleep 30
                        
                        echo "Checking service health..."
                        docker-compose ps
                    '''
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    echo 'Performing health checks...'
                    sh '''
                        echo "Checking service connectivity..."
                        
                        # Check if containers are running
                        cd docker
                        if ! docker-compose ps | grep -q "Up"; then
                            echo "Error: Some services are not running"
                            docker-compose logs
                            exit 1
                        fi
                        
                        echo "All services are running successfully!"
                        
                        # Display running services
                        echo "Current service status:"
                        docker-compose ps
                    '''
                }
            }
        }
        
        stage('Setup Port Forwarding') {
            steps {
                script {
                    echo 'Setting up port forwarding for development access...'
                    sh '''
                        # Create or update dev-helper script
                        cat > dev-helper.bat << 'EOF'
@echo off
if "%1"=="start" (
    echo Starting port forwarding for Digital Banking services...
    echo Frontend will be available at: http://localhost:3000
    echo API Gateway will be available at: http://localhost:8080
    echo.
    echo Press Ctrl+C to stop port forwarding
    start /B kubectl port-forward -n digital-bank svc/digital-banking-frontend 3000:80
    start /B kubectl port-forward -n digital-bank svc/digital-banking-api-gateway 8080:8080
    echo Port forwarding started in background
    pause
) else if "%1"=="stop" (
    echo Stopping all port forwarding...
    taskkill /F /IM kubectl.exe 2>nul
    echo Port forwarding stopped
) else (
    echo Usage: dev-helper.bat [start^|stop]
    echo   start - Start port forwarding for development
    echo   stop  - Stop all port forwarding
)
EOF
                        
                        chmod +x dev-helper.bat
                        echo "Development helper script created: dev-helper.bat"
                    '''
                }
            }
        }
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
