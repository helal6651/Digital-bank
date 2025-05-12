# Watermark Service Deployment

This document outlines the steps to build the Watermark Service JAR file locally, deploy it to the server `v4465a.unx.vstage.co` for both Test and Production environments, and manage the service instances. The current version being deployed is **1.0.1**.

## Table of Contents

1.  [Prerequisites](#prerequisites)
2.  [Building the Application (Locally)](#building-the-application-locally)
3.  [Deployment to Server v4465a.unx.vstage.co](#deployment-to-server-v4465aunxvstageco)
    *   [Test Environment](#test-environment)
    *   [Production Environment](#production-environment)
4.  [Restarting the Service](#restarting-the-service)
5.  [Verification](#verification)
6.  [Configuration](#configuration)
7.  [Scheduled Tasks / Cron Jobs](#scheduled-tasks--cron-jobs)
8.  [Load Balancer (Nginx)](#load-balancer-nginx)
9.  [Rollback Strategy (Basic)](#rollback-strategy-basic)

## Prerequisites

**For Building (Local Machine):**

*   **Java Development Kit (JDK):** Version X.Y or higher (Specify your required Java version, e.g., 11, 17).
*   **Apache Maven:** Version 3.6+ or as required by the project.
*   **CVS Client:** To check out the source code.
*   **CVS Access:** Credentials and access to the CVS repository `<cvs_repository_path>`.

**For Deployment (Server: `v4465a.unx.vstage.co`):**

*   **SSH Access:** SSH access to `v4465a.unx.vstage.co` with a user account (`<your_username>`) having necessary permissions.
*   **Java Runtime Environment (JRE):** Version X.Y or higher installed on `v4465a.unx.vstage.co`.
*   **Permissions:**
    *   Write permissions for `<your_username>` to:
        *   `/data/watermark_service_test/`
        *   `/data/watermark_service/`
    *   Permissions for the application user to write logs to:
        *   `/var/log/watermark-service-test/`
        *   `/var/log/watermark-service/`
*   **Restart Script:** The `start_watermark_service.sh` script must exist in both `/data/watermark_service_test/` and `/data/watermark_service/` directories on `v4465a.unx.vstage.co`.
*   **Tools:** `scp` or `rsync` available on your local machine for transferring the JAR file.

## Building the Application (Locally)

1.  **Check out the source code from CVS:**
    *(Replace `<cvs_module_name>` and `<cvs_repository_path>` with your actual CVS details)*
    ```bash
    # Example CVS checkout command - adapt to your specific CVS setup
    export CVSROOT=<cvs_repository_path> # e.g., :pserver:user@host:/path/to/repo
    cvs login
    cvs checkout <cvs_module_name> # e.g., watermark-service
    cd <cvs_module_name>
    ```

2.  **Navigate to the project's root directory (if not already there):**
    ```bash
    cd /path/to/your/local/watermark-service-project # This is where you checked out the code
    ```

3.  **Clean and build the project using Maven:**
    *(Ensure your project's pom.xml is configured to produce version 1.0.1)*
    ```bash
    mvn clean package
    ```

4.  **Locate the JAR file:** The build process should generate the specific JAR file:
    *   `target/watermark-service-1.0.1.jar`

    *Verify this file exists.*

## Deployment to Server `v4465a.unx.vstage.co`

This process involves transferring the newly built `watermark-service-1.0.1.jar` file to the server and then running the appropriate restart script. The script handles stopping existing instance(s) and starting new one(s).

**Generic Steps:**

1.  Build the `watermark-service-1.0.1.jar` file locally.
2.  Transfer the JAR file to `/tmp/` on `v4465a.unx.vstage.co`.
3.  SSH into `v4465a.unx.vstage.co`.
4.  Navigate to the deployment directory (test or prod).
5.  (Recommended) Backup the existing deployed JAR.
6.  Move the new `watermark-service-1.0.1.jar` from `/tmp/` into the deployment directory, potentially renaming it to a standard name like `watermark-service.jar` if the start script expects that.
7.  Execute the `start_watermark_service.sh` script to perform the restart.
8.  Verify the deployment (see [Verification](#verification)).

---

### Test Environment

*   **Server:** `v4465a.unx.vstage.co`
*   **Deployment Directory:** `/data/watermark_service_test/`
*   **Port:** `8089`
*   **Restart Script:** `/data/watermark_service_test/start_watermark_service.sh`
*   **Log File:** `/var/log/watermark-service-test/instance_8089_logger.log`
*   **Target JAR Name (Example):** `watermark-service.jar` (Script might expect this specific name)

**Example Deployment Steps (Test - Version 1.0.1):**

1.  **Transfer the JAR (from your local machine):**
    Replace `<your_username>` with your server username.
    ```bash
    scp target/watermark-service-1.0.1.jar <your_username>@v4465a.unx.vstage.co:/tmp/
    ```

2.  **SSH into the server:**
    ```bash
    ssh <your_username>@v4465a.unx.vstage.co
    ```

3.  **Navigate, Backup (Optional), and Replace JAR:**
    ```bash
    cd /data/watermark_service_test/
    # Optional but Recommended Backup (assuming script uses 'watermark-service.jar'):
    if [ -f watermark-service.jar ]; then
      mv watermark-service.jar watermark-service.jar.backup_$(date +%Y%m%d_%H%M%S)
    fi
    # Move and rename the new 1.0.1 JAR to the name expected by the script:
    mv /tmp/watermark-service-1.0.1.jar ./watermark-service.jar
    ```

4.  **Restart the service:**
    ```bash
    ./start_watermark_service.sh
    ```
    *(This script in the `/data/watermark_service_test/` directory should restart the instance running on port 8089)*

5.  **Verify:** Check logs and service status for port 8089 (see [Verification](#verification)).
6.  **Exit SSH:** `exit`

---

### Production Environment

*   **Server:** `v4465a.unx.vstage.co`
*   **Deployment Directory:** `/data/watermark_service/`
*   **Ports:** `8081`, `8082`
*   **Restart Script:** `/data/watermark_service/start_watermark_service.sh`
*   **Log Files:**
    *   Instance 8081: `/var/log/watermark-service/instance_8081_logger.log`
    *   Instance 8082: `/var/log/watermark-service/instance_8082_logger.log`
*   **Target JAR Name (Example):** `watermark-service.jar` (Script might expect this specific name)

**⚠️ Caution: Production deployments require extra care. Consider deploying during off-peak hours. If deploying to both instances (8081 and 8082) simultaneously via the script, ensure this brief downtime is acceptable. For zero-downtime, a rolling update strategy involving the load balancer is needed (see [Load Balancer](#load-balancer-nginx) section).**

**Example Deployment Steps (Production - Version 1.0.1):**

1.  **Transfer the JAR (from your local machine):**
    Replace `<your_username>` with your server username.
    ```bash
    scp target/watermark-service-1.0.1.jar <your_username>@v4465a.unx.vstage.co:/tmp/
    ```

2.  **SSH into the server:**
    ```bash
    ssh <your_username>@v4465a.unx.vstage.co
    ```

3.  **Navigate, Backup (Optional), and Replace JAR:**
    ```bash
    cd /data/watermark_service/
    # Optional but Recommended Backup (assuming script uses 'watermark-service.jar'):
    if [ -f watermark-service.jar ]; then
      mv watermark-service.jar watermark-service.jar.backup_$(date +%Y%m%d_%H%M%S)
    fi
    # Move and rename the new 1.0.1 JAR to the name expected by the script:
    mv /tmp/watermark-service-1.0.1.jar ./watermark-service.jar
    ```

4.  **Restart the service(s):**
    *   To restart **all** production instances (8081 & 8082) with the new JAR:
        ```bash
        ./start_watermark_service.sh
        ```
    *   Alternatively, if performing a rolling update (see [Load Balancer](#load-balancer-nginx)):
        ```bash
        # Example: Restart only 8081 after taking it out of LB rotation
        ./start_watermark_service.sh 8081
        # Verify instance 8081... then proceed with 8082
        ```

5.  **Verify:** Check logs and service status on ports 8081 and 8082 (see [Verification](#verification)).
6.  **Exit SSH:** `exit`

## Restarting the Service

The `start_watermark_service.sh` script in each respective directory handles stopping the running instance(s) and starting new ones (using the currently deployed JAR, e.g., `watermark-service.jar`).

*   **Test (on `v4465a.unx.vstage.co`):**
    ```bash
    ssh <your_username>@v4465a.unx.vstage.co
    cd /data/watermark_service_test/
    ./start_watermark_service.sh
    exit
    ```
    *(Restarts the single instance on port 8089)*

*   **Production (on `v4465a.unx.vstage.co`):**
    ```bash
    ssh <your_username>@v4465a.unx.vstage.co
    cd /data/watermark_service/
    ```
    *   To restart **all** production instances (8081 & 8082):
        ```bash
        ./start_watermark_service.sh
        ```
    *   To restart **only** the instance on port **8081**:
        ```bash
        ./start_watermark_service.sh 8081
        ```
    *   To restart **only** the instance on port **8082**:
        ```bash
        ./start_watermark_service.sh 8082
        ```
    ```bash
    exit
    ```

## Verification

After restarting the service, verify it's running correctly on `v4465a.unx.vstage.co`:

1.  **Check Logs:** Tail the specific application logs.
    ```bash
    # SSH into the server first: ssh <your_username>@v4465a.unx.vstage.co

    # --- Test Log ---
    tail -f /var/log/watermark-service-test/instance_8089_logger.log

    # --- Production Logs ---
    # Instance 8081:
    tail -f /var/log/watermark-service/instance_8081_logger.log
    # Instance 8082:
    tail -f /var/log/watermark-service/instance_8082_logger.log

    # --- Monitoring Script Logs ---
    # Check if the monitor script is running correctly or took action:
    tail -f /var/log/monitor_watermark_service.log
    ```

2.  **Check Process Status:**
    ```bash
    # SSH into the server first
    ps aux | grep watermark-service
    # Or more specifically for Java processes
    jps -l | grep watermark-service
    ```

3.  **Check Health Endpoint (if available):** If your application exposes a health check endpoint (e.g., via Spring Boot Actuator):
    *   **Test:** `curl http://v4465a.unx.vstage.co:8089/actuator/health`
    *   **Production Instance 1:** `curl http://v4465a.unx.vstage.co:8081/actuator/health`
    *   **Production Instance 2:** `curl http://v4465a.unx.vstage.co:8082/actuator/health`
    *   **Load Balanced Endpoint:** `curl http://<nginx_load_balancer_address_or_hostname>/actuator/health` (Replace with actual LB address)

    Look for a status indicating "UP". You can also check the `/actuator/info` endpoint (if configured) to potentially see the deployed version `1.0.1`.

## Configuration

*   Environment-specific configurations (ports, database URLs, external service endpoints, log file locations etc.) should be managed *outside* the JAR file.
*   The `start_watermark_service.sh` scripts in `/data/watermark_service_test/` and `/data/watermark_service/` are responsible for passing the correct configuration. This likely involves setting the `logging.file.name` or similar property for the application.
*   Review the scripts and any associated config files (`application-*.properties`, `application-*.yml`) in each directory to understand how parameters are set.
*   External dependencies like **HashiCorp Vault** may be used for secrets management (see Scheduled Tasks).

## Scheduled Tasks / Cron Jobs

Several automated tasks run on `v4465a.unx.vstage.co` via cron that are relevant to the operation and monitoring of the Watermark Service and its dependencies. You can view the crontab with `crontab -l`. Key tasks include:

*   **Watermark Service Monitoring (Production):**
    *   **Schedule:** Every 3 minutes (`*/3 * * * *`)
    *   **Command:** `/data/watermark_service/monitor_watermark_service.sh`
    *   **Purpose:** Monitors the production Watermark Service instances (8081, 8082). Likely attempts to restart them if found unresponsive.
    *   **Log:** `/var/log/monitor_watermark_service.log`

*   **HashiCorp Vault Monitoring:**
    *   **Schedule:** Every 1 minute (`*/1 * * * *`)
    *   **Command:** `/opt/vault_scripts/monitor_hashicorp_vault_service.sh`
    *   **Purpose:** Monitors the HashiCorp Vault service.
    *   **Log:** `/var/log/monitor_hashicorp_vault_service.log`

*   **Vault Key Rotation:**
    *   **Schedule:** Monthly, 1st day at 2:00 AM (`0 2 1 * *`)
    *   **Command:** `/opt/vault_scripts/rotate_rsa_key_pair_password_in_hashicorp_vault_service.sh`
    *   **Purpose:** Rotates security keys within Vault. Sends email alerts on failure.
    *   **Log:** `/var/log/monitor_hashicorp_vault_service.log`

*   **Log Rotation/Cleanup:**
    *   **Schedule:** Quarterly, 1st day at Midnight (`0 0 1 */3 *`)
    *   **Command:** `/opt/scripts/log_rotate_and_cleanup.sh`
    *   **Purpose:** Manages log file sizes by rotating and potentially cleaning up older logs (likely including those in `/var/log/watermark-service*` and `/var/log/monitor*`).

*   **Log Permissions for Monitoring:**
    *   **Schedule:** Hourly, at the top of the hour (`0 * * * *`)
    *   **Command:** `/usr/local/bin/log_permission.sh`
    *   **Purpose:** Ensures log files (including Watermark Service logs at `/var/log/watermark-service*`) are readable by monitoring agents like Promtail for log aggregation in Loki/Grafana.

*   **Grafana Monitoring:**
    *   **Schedule:** Every 1 minute (`* * * * *`)
    *   **Command:** `pgrep grafana || systemctl start grafana-server`
    *   **Purpose:** Ensures the Grafana server is running.

**Note:** Be aware of these automated tasks. Check their respective logs for details on actions taken. The log rotation and permission scripts directly affect the application log files.

## Load Balancer (Nginx)

*   An Nginx instance load balances traffic across production instances (8081, 8082) on `v4465a.unx.vstage.co`.
*   Configuration: `/etc/nginx/nginx.conf` (on the Nginx server, likely `v4465a.unx.vstage.co`).
*   **Rolling Updates:** Follow the procedure outlined previously (remove instance from LB, update/restart, verify, add back, repeat for next instance) using `sudo nginx -s reload` to apply Nginx changes.

## Rollback Strategy (Basic)

If deployment of version `1.0.1` fails:

1.  **Identify failure:** Check specific logs (`/var/log/watermark-service*/instance_*.log`) and health endpoints.
2.  **Stop failed instance(s):** Use `start_watermark_service.sh <port>` or `kill <PID>`. Temporarily disable the monitoring cron job (`crontab -e`) if it interferes.
3.  **Restore previous JAR:**
    On `v4465a.unx.vstage.co`, in the affected directory (`/data/watermark_service_test/` or `/data/watermark_service/`):
    ```bash
    # Example (assuming script uses 'watermark-service.jar'):
    mv watermark-service.jar faulty-watermark-service-1.0.1.jar # Keep the faulty one aside
    mv watermark-service.jar.backup_YYYYMMDD_HHMMSS watermark-service.jar # Restore from backup
    ```
4.  **Restart the service with the old JAR:**
    ```bash
    ./start_watermark_service.sh # Or ./start_watermark_service.sh <port> for prod
    ```
5.  **Verify** the service is functional on the previous version using logs and health checks.
6.  Re-enable the monitoring cron job if disabled.

---

**Note:** Replace placeholders like `<cvs_repository_path>`, `<cvs_module_name>`, `<your_username>`, and config file paths with your actual values. Ensure the `start_watermark_service.sh` scripts correctly handle finding and running the deployed JAR (e.g., `watermark-service.jar`) and configuring the correct log output locations.
