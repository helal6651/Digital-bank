@echo off
REM Digital Bank Development Helper Script for Windows
REM Usage: dev-helper.bat [command]

setlocal enabledelayedexpansion

set NAMESPACE=digital-bank
set ISTIO_NAMESPACE=istio-system
set PORT=8090
set DOMAIN=digital-bank.example.com

if "%1"=="" goto :help
if "%1"=="start" goto :start
if "%1"=="stop" goto :stop
if "%1"=="status" goto :status
if "%1"=="logs" goto :logs
if "%1"=="restart" goto :restart
if "%1"=="hosts" goto :hosts
if "%1"=="health" goto :health
if "%1"=="help" goto :help
goto :help

:start
echo [INFO] Starting port forwarding on port %PORT%...
taskkill /f /im kubectl.exe 2>nul >nul
start /b kubectl port-forward -n %ISTIO_NAMESPACE% svc/istio-ingressgateway %PORT%:80 >nul 2>&1
timeout /t 3 >nul
echo [SUCCESS] Port forwarding started on port %PORT%
echo [INFO] Access your app at: http://%DOMAIN%:%PORT%
goto :end

:stop
echo [INFO] Stopping port forwarding...
taskkill /f /im kubectl.exe 2>nul >nul
echo [SUCCESS] Port forwarding stopped
goto :end

:status
echo [INFO] === DEV ENVIRONMENT STATUS ===
echo.
echo [INFO] Deployments:
kubectl get deployments -n %NAMESPACE%
echo.
echo [INFO] Pods:
kubectl get pods -n %NAMESPACE%
echo.
echo [INFO] Services:
kubectl get svc -n %NAMESPACE%
echo.
goto :end

:logs
set DEPLOYMENT=%2
if "%DEPLOYMENT%"=="" set DEPLOYMENT=digital-banking-frontend
echo [INFO] Showing logs for deployment: %DEPLOYMENT%
kubectl logs -f deployment/%DEPLOYMENT% -n %NAMESPACE%
goto :end

:restart
set DEPLOYMENT=%2
if "%DEPLOYMENT%"=="" set DEPLOYMENT=digital-banking-frontend
echo [INFO] Restarting deployment: %DEPLOYMENT%
kubectl rollout restart deployment/%DEPLOYMENT% -n %NAMESPACE%
kubectl rollout status deployment/%DEPLOYMENT% -n %NAMESPACE%
echo [SUCCESS] Deployment %DEPLOYMENT% restarted
goto :end

:hosts
echo [INFO] Hosts file entry needed:
echo 127.0.0.1 %DOMAIN%
echo.
echo [WARNING] Please add this line to: C:\Windows\System32\drivers\etc\hosts
echo [WARNING] You may need to run as Administrator
goto :end

:health
echo [INFO] Running health checks...
kubectl get pods -n %NAMESPACE% --field-selector=status.phase=Running
echo [INFO] Testing port forward accessibility...
curl -s --max-time 5 http://localhost:%PORT% >nul 2>&1
if !errorlevel! equ 0 (
    echo [SUCCESS] Application is accessible
) else (
    echo [WARNING] Application not responding or port forward not active
)
goto :end

:help
echo Digital Bank Development Helper for Windows
echo Usage: %0 [command]
echo.
echo Commands:
echo   start       - Start port forwarding
echo   stop        - Stop port forwarding
echo   status      - Show environment status
echo   logs [name] - Show logs (default: digital-banking-frontend)
echo   restart [name] - Restart deployment (default: digital-banking-frontend)
echo   hosts       - Show hosts file entry needed
echo   health      - Run health checks
echo   help        - Show this help
echo.
echo Examples:
echo   %0 start
echo   %0 logs user-service
echo   %0 restart account-service
goto :end

:end
endlocal
