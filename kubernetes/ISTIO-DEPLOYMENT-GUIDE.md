# Complete Guide to Deploying Digital Bank with Istio

This guide provides step-by-step instructions for deploying the Digital Bank application with Istio service mesh.

## Step 1: Create a Kubernetes Cluster with Kind

```powershell
# Navigate to your kubernetes folder
cd "C:\Users\BJIT\Desktop\CrossSkillProject\FromBranch\Digital-bank\kubernetes"

# Create a Kind cluster using your custom configuration
kind create cluster --config=overlays/prod/kind-config.yaml
```

## Step 2: Install Istio

```powershell
# Navigate to the Istio installation directory
cd "C:\Users\BJIT\Desktop\CrossSkillProject\FromBranch\Digital-bank\kubernetes\istio-1.22.0\bin"

# Install Istio with the demo profile
.\istioctl.exe install --set profile=demo -y

# Wait for Istio components to be ready
kubectl wait --for=condition=ready pod -l app=istiod -n istio-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=istio-ingressgateway -n istio-system --timeout=300s
```

## Step 3: Create the Digital-Bank Namespace and Enable Istio Injection

```powershell
# Create the digital-bank namespace
kubectl apply -f "C:\Users\BJIT\Desktop\CrossSkillProject\FromBranch\Digital-bank\kubernetes\base\namespace\namespace.yaml"

# Enable Istio injection for the namespace
kubectl label namespace digital-bank istio.io/inject=enabled
```

## Step 4: Deploy Istio Gateway and VirtualService

```powershell
# Apply the Istio Gateway configuration
kubectl apply -f "C:\Users\BJIT\Desktop\CrossSkillProject\FromBranch\Digital-bank\kubernetes\base\istio-gateway\gateway.yaml"

# Apply the hostname VirtualService
kubectl apply -f "C:\Users\BJIT\Desktop\CrossSkillProject\FromBranch\Digital-bank\kubernetes\base\istio-gateway\hostname-virtualservice.yaml"

# Apply the destination rule
kubectl apply -f "C:\Users\BJIT\Desktop\CrossSkillProject\FromBranch\Digital-bank\kubernetes\base\istio-gateway\destinationrule.yaml"
```

## Step 5: Deploy the Application Services

```powershell
# Apply all resources using Kustomize
cd "C:\Users\BJIT\Desktop\CrossSkillProject\FromBranch\Digital-bank\kubernetes"
kubectl apply -k overlays/prod
```

## Step 6: Verify Deployments

```powershell
# Check that all pods are running
kubectl get pods -n digital-bank

# Check all services
kubectl get services -n digital-bank

# Check that the istio-proxy sidecar is injected in your pods
kubectl get pods -n digital-bank -o jsonpath='{.items[*].spec.containers[*].name}' | tr ' ' '\n' | grep istio-proxy
```

## Step 7: Install Istio Addons (Kiali, Prometheus, Grafana, and Jaeger)

```powershell
# Navigate to the Istio samples directory
cd "C:\Users\BJIT\Desktop\CrossSkillProject\FromBranch\Digital-bank\kubernetes\istio-1.22.0\samples\addons"

# Install Kiali
kubectl apply -f kiali.yaml

# Install Prometheus
kubectl apply -f prometheus.yaml

# Install Grafana
kubectl apply -f grafana.yaml

# Install Jaeger
kubectl apply -f jaeger.yaml

# Wait for the addons to be ready
kubectl wait --for=condition=ready pod -l app=kiali -n istio-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=prometheus -n istio-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=grafana -n istio-system --timeout=300s
```

## Step 8: Set up Host File Entry for Hostname-Based Routing

```powershell
# Run PowerShell as Administrator and add a hosts file entry
Start-Process powershell -Verb RunAs -ArgumentList "-Command Add-Content -Path 'C:\Windows\System32\drivers\etc\hosts' -Value '127.0.0.1 digital-bank.example.com' -Force"
```

## Step 9: Access the Application

```powershell
# Port-forward the Istio ingress gateway to access services
kubectl port-forward -n istio-system svc/istio-ingressgateway 8080:80
```

Now you can access:
- Frontend: http://digital-bank.example.com:8080/
- User API: http://digital-bank.example.com:8080/v1/api/user/
- Authentication: http://digital-bank.example.com:8080/v1/api/authenticate

## Step 10: Access Kiali Dashboard for Service Mesh Visualization

```powershell
# Port-forward the Kiali service
kubectl port-forward -n istio-system svc/kiali 20001:20001
```

Access the Kiali dashboard at: http://localhost:20001/

## Step 11: Access Other Dashboards

### Grafana
```powershell
kubectl port-forward -n istio-system svc/grafana 3000:3000
```
Access at: http://localhost:3000/

### Jaeger (Distributed Tracing)
```powershell
kubectl port-forward -n istio-system svc/jaeger-collector 16686:16686
```
Access at: http://localhost:16686/

### Prometheus
```powershell
kubectl port-forward -n istio-system svc/prometheus 9090:9090
```
Access at: http://localhost:9090/

## Step 12: Generate Traffic for Visualization

To see meaningful data in Kiali and other dashboards, generate some traffic to your services:

```powershell
# Use a loop to send requests to your API endpoints
for ($i=0; $i -lt 50; $i++) {
    Invoke-WebRequest -Uri "http://digital-bank.example.com:8080/v1/api/user/" -Method GET -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
}
```

## Step 13: Check for Issues in the Service Mesh

```powershell
# Use Istio's built-in analyzer to check for configuration issues
cd "C:\Users\BJIT\Desktop\CrossSkillProject\FromBranch\Digital-bank\kubernetes\istio-1.22.0\bin"
.\istioctl.exe analyze -n digital-bank
```

## Step 14: Clean Up When Done

```powershell
# Delete the Kind cluster when you're done
kind delete cluster
```

## Key Configuration Files

### Gateway (gateway.yaml)
```yaml
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: digital-bank-gateway
  namespace: digital-bank
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "digital-bank.example.com"
    - "*"
```

### VirtualService (hostname-virtualservice.yaml)
```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: digital-bank-hostname
  namespace: digital-bank
spec:
  hosts:
  - "digital-bank.example.com"
  gateways:
  - digital-bank-gateway
  http:
  - match:
    - uri:
        prefix: /v1/api/user/
    route:
    - destination:
        host: user-service
        port:
          number: 9491
  - match:
    - uri:
        prefix: /v1/api/authenticate
    route:
    - destination:
        host: user-service
        port:
          number: 9491
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: digital-banking-frontend
        port:
          number: 80
```

### DestinationRule (destinationrule.yaml)
```yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: digital-banking-frontend
  namespace: digital-bank
spec:
  host: digital-banking-frontend
  trafficPolicy:
    loadBalancer:
      simple: ROUND_ROBIN
```

## Advanced Istio Features

### 1. Traffic Shifting (Canary Deployment)

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: user-service-canary
  namespace: digital-bank
spec:
  hosts:
  - "digital-bank.example.com"
  gateways:
  - digital-bank-gateway
  http:
  - match:
    - uri:
        prefix: /v1/api/user/
    route:
    - destination:
        host: user-service
        port:
          number: 9491
        subset: v1
      weight: 90
    - destination:
        host: user-service
        port:
          number: 9491
        subset: v2
      weight: 10
```

### 2. Circuit Breaking

```yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: circuit-breaker
  namespace: digital-bank
spec:
  host: user-service
  trafficPolicy:
    connectionPool:
      http:
        http1MaxPendingRequests: 1
        maxRequestsPerConnection: 1
      tcp:
        maxConnections: 1
    outlierDetection:
      consecutive5xxErrors: 1
      interval: 1s
      baseEjectionTime: 3m
      maxEjectionPercent: 100
```

### 3. Fault Injection

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: user-service-delay
  namespace: digital-bank
spec:
  hosts:
  - "digital-bank.example.com"
  gateways:
  - digital-bank-gateway
  http:
  - match:
    - uri:
        prefix: /v1/api/user/
    fault:
      delay:
        percentage:
          value: 10
        fixedDelay: 5s
    route:
    - destination:
        host: user-service
        port:
          number: 9491
```

### 4. Mutual TLS

To verify that mTLS is enabled:

```powershell
# Check mTLS status
.\istioctl.exe authn tls-check user-service.digital-bank
```

To explicitly enable strict mTLS:

```yaml
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: digital-bank
spec:
  mtls:
    mode: STRICT
```

## Troubleshooting Tips

1. **Check Istio Proxy Logs**:
   ```powershell
   kubectl logs -n digital-bank <pod-name> -c istio-proxy
   ```

2. **Check if Istio is Injecting Sidecars**:
   ```powershell
   kubectl get pod <pod-name> -n digital-bank -o yaml | Select-String -Pattern "istio-proxy"
   ```

3. **Verify VirtualService Configuration**:
   ```powershell
   .\istioctl.exe describe vs digital-bank-hostname -n digital-bank
   ```

4. **Check Endpoint Configuration**:
   ```powershell
   kubectl get endpoints -n digital-bank
   ```

5. **Debug Traffic Flow**:
   ```powershell
   # Start a temporary debug container
   kubectl debug -n digital-bank deployment/user-service --image=nicolaka/netshoot -- sh
   ```

6. **Enable Istio Debug Logs**:
   ```powershell
   # Increase log level for Envoy proxies
   .\istioctl.exe install --set profile=demo --set meshConfig.accessLogFile="/dev/stdout" --set values.global.proxy.logLevel=debug -y
   ```

7. **Verify Service Entries in Kiali**:
   Access the Kiali dashboard and check for any misconfigurations in the Graph view.

## Common Issues and Solutions

1. **Pods not getting Istio sidecar injected**
   - Ensure the namespace has the `istio-injection=enabled` label
   - Restart deployments after adding the label: `kubectl rollout restart deployment -n digital-bank`

2. **503 Service Unavailable errors**
   - Check if the backend service is running: `kubectl get pods -n digital-bank`
   - Verify service endpoints: `kubectl get endpoints -n digital-bank`
   - Check for routing issues in VirtualService

3. **Cannot access services via Istio Gateway**
   - Verify Gateway and VirtualService configurations
   - Check that port forwarding is working: `kubectl port-forward -n istio-system svc/istio-ingressgateway 8080:80`
   - Verify hosts file entry: `ping digital-bank.example.com`

4. **Kiali shows no data**
   - Ensure Prometheus is running: `kubectl get pods -n istio-system | grep prometheus`
   - Generate some traffic to your services

## Additional Resources

- [Istio Documentation](https://istio.io/latest/docs/)
- [Kiali Documentation](https://kiali.io/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/home/)

## Project Maintenance

For ongoing maintenance and updates:

1. **Updating Istio**:
   ```powershell
   # Download new Istio version
   # Update with proper upgrade procedure
   .\istioctl.exe upgrade
   ```

2. **Adding New Services**:
   - Add service deployments to the base folder
   - Update VirtualService to include new routing rules
   - Update kustomization.yaml files as needed

3. **Scaling Services**:
   ```powershell
   kubectl scale deployment user-service -n digital-bank --replicas=3
   ```

4. **Monitoring Health**:
   - Use Kiali for service health visualization
   - Set up Grafana dashboards for detailed metrics
   - Configure alerts in Prometheus
