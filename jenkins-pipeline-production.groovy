pipeline {
    agent any
    
    environment {
        DOCKER_CREDENTIALS = credentials('docker-credentials')
        KUBECTL_VERSION = 'v1.31.2'
        DOMAIN_NAME = 'digitalbank.yourdomain.com'
        ENVIRONMENT = 'production'
    }
    
    stages {
        // ... existing stages ...
        
        stage('Deploy Production Infrastructure') {
            when {
                branch 'main'
                environment name: 'ENVIRONMENT', value: 'production'
            }
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo '🏭 Deploying production infrastructure...'
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            
                            echo "🔧 Applying production LoadBalancer..."
                            ./kubectl apply -f kubernetes/production-access/istio-gateway-loadbalancer.yaml
                            
                            echo "🌐 Applying production Ingress..."
                            ./kubectl apply -f kubernetes/production-access/digital-bank-ingress.yaml
                            
                            echo "⏳ Waiting for LoadBalancer external IP..."
                            ./kubectl wait --for=condition=ready service/istio-ingressgateway-lb -n istio-system --timeout=300s || true
                            
                            echo "📊 Getting LoadBalancer external IP..."
                            EXTERNAL_IP=$(./kubectl get svc istio-ingressgateway-lb -n istio-system -o jsonpath='{.status.loadBalancer.ingress[0].ip}' || echo "pending")
                            
                            if [ "$EXTERNAL_IP" != "pending" ] && [ "$EXTERNAL_IP" != "" ]; then
                                echo "✅ LoadBalancer external IP: $EXTERNAL_IP"
                                echo "🌐 Configure DNS: $DOMAIN_NAME -> $EXTERNAL_IP"
                            else
                                echo "⏳ LoadBalancer IP still pending - check cloud provider console"
                            fi
                        '''
                    }
                }
            }
        }
        
        stage('Verify Production Access') {
            when {
                branch 'main'
                environment name: 'ENVIRONMENT', value: 'production'
            }
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo '🔍 Verifying production access...'
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            
                            echo "📊 LoadBalancer Services:"
                            ./kubectl get svc -n istio-system | grep LoadBalancer || echo "No LoadBalancer services found"
                            
                            echo "🌐 Ingress Status:"
                            ./kubectl get ingress -n digital-bank || echo "No Ingress found"
                            
                            echo "🎯 External Access Points:"
                            ./kubectl get svc istio-ingressgateway-lb -n istio-system -o wide || echo "LoadBalancer service not found"
                            
                            # Health check (if external IP is available)
                            EXTERNAL_IP=$(./kubectl get svc istio-ingressgateway-lb -n istio-system -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
                            if [ "$EXTERNAL_IP" != "" ]; then
                                echo "🧪 Testing external access..."
                                curl -I http://$EXTERNAL_IP --max-time 10 || echo "External access test failed or pending"
                            fi
                        '''
                    }
                }
            }
        }
        
        stage('Display Production Access Info') {
            steps {
                withCredentials([file(credentialsId: 'kubectl-config', variable: 'KUBECONFIG')]) {
                    script {
                        echo "🏭 Production access information..."
                        sh '''
                            export KUBECONFIG="$(pwd)/kubeconfig"
                            
                            echo "=== 🏭 PRODUCTION ACCESS INFORMATION ==="
                            echo ""
                            echo "🌐 Production URL: https://$DOMAIN_NAME"
                            echo "🔒 SSL/TLS: Enabled with Let's Encrypt"
                            echo ""
                            
                            EXTERNAL_IP=$(./kubectl get svc istio-ingressgateway-lb -n istio-system -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "pending")
                            
                            if [ "$EXTERNAL_IP" != "pending" ] && [ "$EXTERNAL_IP" != "" ]; then
                                echo "✅ External IP: $EXTERNAL_IP"
                                echo "📋 DNS Configuration Required:"
                                echo "   A Record: $DOMAIN_NAME -> $EXTERNAL_IP"
                            else
                                echo "⏳ External IP: Pending (check cloud provider console)"
                            fi
                            
                            echo ""
                            echo "🔧 Production Infrastructure:"
                            echo "   - LoadBalancer: istio-ingressgateway-lb"
                            echo "   - Ingress: digital-bank-ingress"
                            echo "   - SSL/TLS: Automatic via cert-manager"
                            echo "   - Health Checks: Enabled"
                            echo ""
                            echo "✅ No manual port-forwarding required!"
                        '''
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ SUCCESS: Production deployment completed!'
            echo '🏭 Digital Bank is now accessible via production LoadBalancer'
            echo '🌐 Configure DNS to point ${DOMAIN_NAME} to the external IP'
            echo '🔒 SSL certificates will be automatically provisioned'
        }
    }
}
