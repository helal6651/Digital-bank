# Digital Bank - Development Environment Guide

## 🔧 Development Setup and Testing

This guide covers the **recommended development approach** for testing the Digital Bank application locally.

## 🎯 Current Setup (Perfect for Dev)

### ✅ What Works Great in Development:
- **Jenkins Pipeline with Port-Forward** - Automated deployment + immediate access
- **KIND Cluster** - Lightweight Kubernetes for local development
- **Port 8090** - Verified available and conflict-free
- **Istio Gateway** - Production-like routing in dev environment

## 🚀 Quick Start

### 1. Run Jenkins Pipeline
Your current `jenkins-pipeline-merged.groovy` is **optimized for development**:
- ✅ Builds and deploys all services
- ✅ Automatically starts port forwarding on port 8090
- ✅ Provides comprehensive dev status information
- ✅ Includes debugging commands and health checks

### 2. Add Hosts Entry (One-time)
```bash
# Windows
echo 127.0.0.1 digital-bank.example.com >> C:\Windows\System32\drivers\etc\hosts

# Linux/macOS
echo "127.0.0.1 digital-bank.example.com" | sudo tee -a /etc/hosts
```

### 3. Access Your Application
After pipeline completion:
- 🌐 **Frontend**: http://digital-bank.example.com:8090
- 🔍 **Direct**: http://localhost:8090

## 🛠️ Development Helper Tools

### Option 1: Use Helper Scripts

**Windows:**
```cmd
# Start port forwarding
dev-helper.bat start

# Check status
dev-helper.bat status

# View logs
dev-helper.bat logs

# Restart frontend
dev-helper.bat restart

# Stop port forwarding
dev-helper.bat stop
```

**Linux/macOS:**
```bash
# Make executable
chmod +x dev-helper.sh

# Start port forwarding
./dev-helper.sh start

# Check status
./dev-helper.sh status

# View logs
./dev-helper.sh logs

# Stop port forwarding
./dev-helper.sh stop
```

### Option 2: Manual Commands

```bash
# Start port forwarding
kubectl port-forward -n istio-system svc/istio-ingressgateway 8090:80

# Check deployment status
kubectl get pods -n digital-bank
kubectl get svc -n digital-bank

# View frontend logs
kubectl logs -f deployment/digital-banking-frontend -n digital-bank

# View user service logs
kubectl logs -f deployment/user-service -n digital-bank

# Restart a deployment (to pick up new images)
kubectl rollout restart deployment/digital-banking-frontend -n digital-bank
```

## 🧪 Development Workflow

### 1. Code Changes
```bash
# Make your code changes
git add .
git commit -m "Your changes"
git push origin main
```

### 2. Run Pipeline
- Trigger Jenkins pipeline (manually or via webhook)
- Pipeline will automatically:
  - Build new Docker images
  - Push to DockerHub with new tags
  - Deploy to Kubernetes
  - Restart deployments with fresh images
  - Start port forwarding on 8090

### 3. Test Changes
- Access: http://digital-bank.example.com:8090
- Test your changes
- Check logs if needed
- Repeat cycle

## 🔍 Debugging and Troubleshooting

### Common Dev Issues:

**1. Port 8090 Already in Use:**
```bash
# Windows
taskkill /f /im kubectl.exe
netstat -ano | findstr :8090

# Linux/macOS
pkill -f "kubectl.*port-forward.*8090"
lsof -ti:8090 | xargs kill -9
```

**2. Application Not Loading:**
```bash
# Check if pods are running
kubectl get pods -n digital-bank

# Check if services are ready
kubectl get svc -n digital-bank

# Check specific pod logs
kubectl logs -f <pod-name> -n digital-bank
```

**3. Port Forward Not Working:**
```bash
# Restart port forward
kubectl port-forward -n istio-system svc/istio-ingressgateway 8090:80

# Check if Istio gateway is running
kubectl get svc -n istio-system
```

**4. DNS Not Resolving:**
```bash
# Verify hosts file entry
ping digital-bank.example.com

# Should resolve to 127.0.0.1
```

## 📊 Development vs Production

| Aspect | Development (Current) | Production |
|--------|----------------------|------------|
| **Access Method** | `kubectl port-forward` ✅ | LoadBalancer + Ingress |
| **Domain** | `digital-bank.example.com:8090` | `yourcompany.com` |
| **SSL/TLS** | Not needed | Required (cert-manager) |
| **DNS** | Hosts file entry | Real DNS configuration |
| **Infrastructure** | KIND cluster | Cloud Kubernetes (EKS/AKS/GKE) |
| **Cost** | Free | Cloud provider costs |
| **Setup Time** | Immediate | Hours (DNS, SSL, LB) |

## ✅ Why Your Current Approach is Perfect for Dev

1. **Immediate Access** - No waiting for DNS/SSL setup
2. **No External Dependencies** - Everything runs locally
3. **Easy Debugging** - Direct kubectl access to logs and pods
4. **Cost Effective** - No cloud resources needed
5. **Isolated Environment** - No conflicts with other developers
6. **Realistic Testing** - Uses same Istio/Kubernetes as production

## 🎯 Development Best Practices

### 1. **Use the Pipeline** - Don't deploy manually
```bash
# ❌ Don't do this in dev
kubectl apply -f kubernetes/

# ✅ Use pipeline instead
# Trigger jenkins-pipeline-merged.groovy
```

### 2. **Monitor Resource Usage**
```bash
# Check cluster resources
kubectl top nodes
kubectl top pods -n digital-bank
```

### 3. **Clean Up When Done**
```bash
# Stop port forwarding
taskkill /f /im kubectl.exe  # Windows
pkill -f kubectl             # Linux/macOS

# Optional: Clean up deployments
kubectl delete namespace digital-bank
```

### 4. **Use Staging Before Production**
- Test thoroughly in dev (current setup)
- Deploy to staging with production-like setup
- Only then deploy to production

## 🚀 Next Steps

Your development environment is **production-ready** for testing! 

**For Production Migration:**
1. Keep current dev setup for daily development
2. Use `jenkins-pipeline-production.groovy` for staging/production
3. Set up real domain and SSL certificates
4. Configure cloud LoadBalancer

**Happy Development! 🎉**
