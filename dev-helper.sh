#!/bin/bash
# Digital Bank Development Helper Script
# Usage: ./dev-helper.sh [command]

set -e

NAMESPACE="digital-bank"
ISTIO_NAMESPACE="istio-system"
PORT="8090"
DOMAIN="digital-bank.example.com"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if kubectl is available
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed or not in PATH"
        exit 1
    fi
}

# Function to check if cluster is accessible
check_cluster() {
    if ! kubectl cluster-info &> /dev/null; then
        print_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    print_success "Connected to Kubernetes cluster"
}

# Function to start port forwarding
start_port_forward() {
    print_status "Starting port forwarding on port $PORT..."
    
    # Kill existing port-forward if any
    pkill -f "kubectl.*port-forward.*$PORT" 2>/dev/null || true
    
    # Start new port-forward
    kubectl port-forward -n $ISTIO_NAMESPACE svc/istio-ingressgateway $PORT:80 > /dev/null 2>&1 &
    PF_PID=$!
    
    # Save PID
    echo $PF_PID > port-forward.pid
    
    sleep 3
    
    if ps -p $PF_PID > /dev/null 2>&1; then
        print_success "Port forwarding started on port $PORT (PID: $PF_PID)"
        print_status "Access your app at: http://$DOMAIN:$PORT"
    else
        print_error "Failed to start port forwarding"
        exit 1
    fi
}

# Function to stop port forwarding
stop_port_forward() {
    if [ -f port-forward.pid ]; then
        PID=$(cat port-forward.pid)
        if ps -p $PID > /dev/null 2>&1; then
            kill -9 $PID
            print_success "Port forwarding stopped (PID: $PID)"
        else
            print_warning "Port forward process not running"
        fi
        rm -f port-forward.pid
    else
        print_warning "No port-forward PID file found"
    fi
}

# Function to show status
show_status() {
    print_status "=== DEV ENVIRONMENT STATUS ==="
    echo
    
    print_status "Deployments:"
    kubectl get deployments -n $NAMESPACE
    echo
    
    print_status "Pods:"
    kubectl get pods -n $NAMESPACE
    echo
    
    print_status "Services:"
    kubectl get svc -n $NAMESPACE
    echo
    
    print_status "Port Forward Status:"
    if [ -f port-forward.pid ]; then
        PID=$(cat port-forward.pid)
        if ps -p $PID > /dev/null 2>&1; then
            print_success "Active on PID: $PID - http://localhost:$PORT"
        else
            print_error "Port forward process not running"
        fi
    else
        print_warning "No port-forward active"
    fi
}

# Function to show logs
show_logs() {
    DEPLOYMENT=${1:-digital-banking-frontend}
    print_status "Showing logs for deployment: $DEPLOYMENT"
    kubectl logs -f deployment/$DEPLOYMENT -n $NAMESPACE
}

# Function to restart deployment
restart_deployment() {
    DEPLOYMENT=${1:-digital-banking-frontend}
    print_status "Restarting deployment: $DEPLOYMENT"
    kubectl rollout restart deployment/$DEPLOYMENT -n $NAMESPACE
    kubectl rollout status deployment/$DEPLOYMENT -n $NAMESPACE
    print_success "Deployment $DEPLOYMENT restarted"
}

# Function to setup hosts file entry
setup_hosts() {
    HOSTS_ENTRY="127.0.0.1 $DOMAIN"
    HOSTS_FILE="/etc/hosts"
    
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
        HOSTS_FILE="/c/Windows/System32/drivers/etc/hosts"
    fi
    
    if grep -q "$DOMAIN" "$HOSTS_FILE" 2>/dev/null; then
        print_success "Hosts entry already exists"
    else
        print_status "Adding hosts entry (may require sudo):"
        print_status "echo '$HOSTS_ENTRY' >> $HOSTS_FILE"
        print_warning "Please run: echo '$HOSTS_ENTRY' | sudo tee -a $HOSTS_FILE"
    fi
}

# Function to run health check
health_check() {
    print_status "Running health checks..."
    
    # Check if pods are running
    RUNNING_PODS=$(kubectl get pods -n $NAMESPACE --field-selector=status.phase=Running --no-headers | wc -l)
    TOTAL_PODS=$(kubectl get pods -n $NAMESPACE --no-headers | wc -l)
    
    print_status "Running pods: $RUNNING_PODS/$TOTAL_PODS"
    
    # Check if port forward is accessible
    if [ -f port-forward.pid ]; then
        PID=$(cat port-forward.pid)
        if ps -p $PID > /dev/null 2>&1; then
            if curl -s --max-time 5 http://localhost:$PORT > /dev/null 2>&1; then
                print_success "Application is accessible via port forward"
            else
                print_warning "Port forward active but application not responding"
            fi
        else
            print_error "Port forward not running"
        fi
    else
        print_warning "No port forward active"
    fi
}

# Function to show help
show_help() {
    echo "Digital Bank Development Helper"
    echo "Usage: $0 [command]"
    echo
    echo "Commands:"
    echo "  start       - Start port forwarding"
    echo "  stop        - Stop port forwarding"
    echo "  status      - Show environment status"
    echo "  logs [name] - Show logs (default: digital-banking-frontend)"
    echo "  restart [name] - Restart deployment (default: digital-banking-frontend)"
    echo "  hosts       - Setup hosts file entry"
    echo "  health      - Run health checks"
    echo "  help        - Show this help"
    echo
    echo "Examples:"
    echo "  $0 start"
    echo "  $0 logs user-service"
    echo "  $0 restart account-service"
}

# Main script logic
check_kubectl
check_cluster

case "${1:-help}" in
    start)
        start_port_forward
        ;;
    stop)
        stop_port_forward
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs $2
        ;;
    restart)
        restart_deployment $2
        ;;
    hosts)
        setup_hosts
        ;;
    health)
        health_check
        ;;
    help|*)
        show_help
        ;;
esac
