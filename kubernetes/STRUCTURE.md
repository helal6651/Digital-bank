# Kubernetes Configuration Structure

```
kubernetes/
│
├── README.md                   # Documentation for Kubernetes deployment
│
├── base/                       # Base configurations for all services
│   │
│   ├── account-service/        # Account Service configuration
│   │   ├── deployment.yaml     # Deployment spec for Account Service
│   │   ├── kustomization.yaml  # Kustomize config for Account Service
│   │   └── service.yaml        # Service definition for Account Service
│   │
│   ├── api-gateway/            # API Gateway configuration
│   │   ├── deployment.yaml     # Deployment spec for API Gateway
│   │   ├── kustomization.yaml  # Kustomize config for API Gateway
│   │   └── service.yaml        # Service definition for API Gateway
│   │
│   ├── front-end/              # Front-end application configuration
│   │   ├── deployment.yaml     # Deployment spec for Front-end
│   │   ├── kustomization.yaml  # Kustomize config for Front-end
│   │   └── service.yaml        # Service definition for Front-end
│   │
│   ├── ingress-controller/     # Ingress Controller configuration
│   │   ├── ingress.yaml        # Ingress resource configuration
│   │   └── kustomization.yaml  # Kustomize config for Ingress
│   │
│   ├── kafdrop/                # Kafdrop (Kafka UI) configuration
│   │   ├── deployment.yaml     # Deployment spec for Kafdrop
│   │   ├── kustomization.yaml  # Kustomize config for Kafdrop
│   │   └── service.yaml        # Service definition for Kafdrop
│   │
│   ├── kafka/                  # Kafka message broker configuration
│   │   ├── configmap.yaml      # ConfigMap for Kafka configuration
│   │   ├── deployment.yaml     # Deployment spec for Kafka
│   │   ├── kustomization.yaml  # Kustomize config for Kafka
│   │   ├── pvc.yaml            # Persistent Volume Claim for Kafka
│   │   └── service.yaml        # Service definition for Kafka
│   │
│   ├── namespace/              # Namespace configuration
│   │   ├── kustomization.yaml  # Kustomize config for Namespace
│   │   └── namespace.yaml      # Namespace definition
│   │
│   ├── notification-service/   # Notification Service configuration
│   │   ├── deployment.yaml     # Deployment spec for Notification Service
│   │   ├── kustomization.yaml  # Kustomize config for Notification Service
│   │   └── service.yaml        # Service definition for Notification Service
│   │
│   ├── postgres/               # PostgreSQL database configuration
│   │   ├── deployment.yaml     # Deployment spec for PostgreSQL
│   │   ├── kustomization.yaml  # Kustomize config for PostgreSQL
│   │   ├── pvc.yaml            # Persistent Volume Claim for PostgreSQL
│   │   ├── secret.yaml         # Secret for PostgreSQL credentials
│   │   └── service.yaml        # Service definition for PostgreSQL
│   │
│   ├── redis/                  # Redis cache configuration
│   │   ├── deployment.yaml     # Deployment spec for Redis
│   │   ├── kustomization.yaml  # Kustomize config for Redis
│   │   ├── pvc.yaml            # Persistent Volume Claim for Redis
│   │   └── service.yaml        # Service definition for Redis
│   │
│   ├── user-service/           # User Service configuration
│   │   ├── deployment.yaml     # Deployment spec for User Service
│   │   ├── kustomization.yaml  # Kustomize config for User Service
│   │   └── service.yaml        # Service definition for User Service
│   │
│   └── vault/                  # HashiCorp Vault configuration
│       ├── deployment.yaml     # Deployment spec for Vault
│       ├── kustomization.yaml  # Kustomize config for Vault
│       ├── rbac.yaml           # RBAC configuration for Vault
│       ├── service.yaml        # Service definition for Vault
│       ├── vault-config-configmap.yaml # ConfigMap for Vault config
│       ├── vault-init-script-configmap.yaml # ConfigMap for Vault init script
│       └── vault-pvc.yaml      # Persistent Volume Claim for Vault
│
└── overlays/                   # Environment-specific configurations
    │
    ├── dev/                    # Development environment
    │   ├── config.yaml         # Development-specific configuration
    │   └── kustomization.yaml  # Kustomize config for dev environment
    │
    └── prod/                   # Production environment
        ├── config.yaml         # Production-specific configuration
        ├── kind-config.yaml    # Kind cluster configuration for production
        └── kustomization.yaml  # Kustomize config for prod environment
```

## Deployment Instructions

To deploy the application:

1. For development environment:
   ```bash
   kubectl apply -k overlays/dev
   ```

2. For production environment:
   ```bash
   kubectl apply -k overlays/prod
   ```

## Notes

- The base directory contains all the core Kubernetes manifests
- The overlays directory contains environment-specific patches and configurations
- Kustomize is used for configuration management and customization across environments
