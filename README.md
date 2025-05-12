# Watermark Service Deployment

This document outlines the steps to build the Watermark Service JAR file locally, deploy it to the server `v4465a.unx.vstage.co` for both Test and Production environments, and manage the service instances using `sudo`. The current version being deployed is **1.0.1**, requiring **Java 17**.

## Table of Contents

1.  [Prerequisites](#prerequisites)
2.  [Building the Application (Locally)](#building-the-application-locally)
3.  [Deployment to Server v4465a.unx.vstage.co](#deployment-to-server-v4465aunxvstageco)
    *   [Test Environment](#test-environment)
    *   [Production Environment](#production-environment)
4.  [Restarting the Service (Using Sudo)](#restarting-the-service-using-sudo)
5.  [Verification](#verification)
6.  [Configuration](#configuration)
7.  [Scheduled Tasks / Cron Jobs](#scheduled-tasks--cron-jobs)
8.  [Load Balancer (Nginx)](#load-balancer-nginx)
9.  [Rollback Strategy (Basic)](#rollback-strategy-basic)

## Prerequisites

**For Building (Local Machine):**

*   **Java Development Kit (JDK):** **Version 17** or higher.
*   **Apache Maven:** Version 3.6+ or as required by the project.
*   **CVS Client:** To check out the source code.
*   **CVS Access:** Credentials and access to the project's CVS repository.

**For Deployment (Server: `v4465a.unx.vstage.co`):**

*   **SSH Access:** SSH access to `v4465a.unx.vstage.co` with a user account (`<your_username>`).
*   **Sudo Privileges:** The user `<your_username>` must have `sudo` privileges to execute the `start_watermark_service.sh` script.
*   **Java Runtime Environment (JRE):** **Version 17** or higher installed on `v4465a.unx.vstage.co`.
*   **Permissions:**
    *   Write permissions for `<your_username>` to:
        *   `/data/watermark_service_test/`
        *   `/data/watermark_service/`
    *   The user the service runs as (potentially `root` if started via `sudo` without user switching, or a specific user defined in the script) needs permissions to write logs to:
        *   `/var/log/watermark-service-test/`
        *   `/var/log/watermark-service/` (See Cron Job for log permissions).
*   **Restart Script:** The `start_watermark_service.sh` script must exist in both `/data/watermark_service_test/` and `/data/watermark_service/` directories and be executable. **Crucially, this script must be able to identify and run the latest versioned JAR file (e.g., `watermark-service-1.0.1.jar`) within its directory.**
*   **Tools:** `scp` or `rsync` available on your local machine for transferring the JAR file.

## Building the Application (Locally)

1.  **Ensure Java 17 is active:**
    ```bash
    java -version # Verify it shows Java 17
    # Set JAVA_HOME or use SDK management tools if needed
    ```
2.  **Check out the source code from CVS:**
    Use your standard CVS client commands to check out the latest version of the Watermark Service source code to your local machine.

3.  **Navigate to the project's root directory:**
    ```bash
    cd /path/to/your/local/watermark-service-project # This is where you checked out the code
    ```
4.  **Clean and build the project using Maven:**
    *(Ensure your project's pom.xml is configured to produce version 1.0.1 and uses Java 17)*
    ```bash
    mvn clean package
    ```
5.  **Locate the JAR file:** The build process should generate the specific JAR file:
    *   `target/watermark-service-1.0.1.jar`

    *Verify this file exists.*

## Deployment to Server `v4465a.unx.vstage.co`

This process involves transferring the newly built `watermark-service-1.0.1.jar` file to the server and then running the restart script **using `sudo`**. The script handles stopping existing instance(s) and starting new one(s) using the newly transferred versioned JAR. **The JAR file is NOT renamed.**

**Generic Steps:**

1.  Build the `watermark-service-1.0.1.jar` file locally using Java 17.
2.  Transfer the `watermark-service-1.0.1.jar` file to `/tmp/` on `v4465a.unx.vstage.co`.
3.  SSH into `v4465a.unx.vstage.co`.
4.  Navigate to the deployment directory (test or prod).
5.  (Recommended) Backup any *previous* version JAR file if needed for rollback.
6.  Move the new `watermark-service-1.0.1.jar` from `/tmp/` into the deployment directory. **Do not rename it.**
7.  Execute the `start_watermark_service.sh` script using `sudo` to perform the restart.
8.  Verify the deployment (see [Verification](#verification)).

---

### Test Environment

*   **Server:** `v4465a.unx.vstage.co`
*   **Deployment Directory:** `/data/watermark_service_test/`
*   **Port:** `8089`
*   **Restart Script:** `/data/watermark_service_test/start_watermark_service.sh` (must find latest JAR)
*   **Log File:** `/var/log/watermark-service-test/instance_8089_logger.log`
*   **Deployed JAR Name:** `watermark-service-1.0.1.jar`

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

3.  **Navigate and Move JAR (No Rename):**
    ```bash
    cd /data/watermark_service_test/
    # Optional: Backup or remove older JAR versions if necessary for script logic or disk space
    # mv watermark-service-1.0.0.jar watermark-service-1.0.0.jar.backup
    # Move the new 1.0.1 JAR into the directory:
    mv /tmp/watermark-service-1.0.1.jar ./
    ```

4.  **Restart the service using sudo:**
    ```bash
    sudo ./start_watermark_service.sh
    ```
    *(This script, run as root, in `/data/watermark_service_test/` must identify and run `watermark-service-1.0.1.jar` and restart the instance on port 8089)*

5.  **Verify:** Check logs and service status for port 8089 (see [Verification](#verification)). Check which JAR the running process is using (`ps aux | grep watermark-service`).
6.  **Exit SSH:** `exit`

---

### Production Environment

*   **Server:** `v4465a.unx.vstage.co`
*   **Deployment Directory:** `/data/watermark_service/`
*   **Ports:** `8081`, `8082`
*   **Restart Script:** `/data/watermark_service/start_watermark_service.sh` (must find latest JAR)
*   **Log Files:**
    *   Instance 8081: `/var/log/watermark-service/instance_8081_logger.log`
    *   Instance 8082: `/var/log/watermark-service/instance_8082_logger.log`
*   **Deployed JAR Name:** `watermark-service-1.0.1.jar`

**⚠️ Caution: Production deployments require extra care. Running start scripts with `sudo` adds risk. Ensure the script is secure and well-tested. Consider deploying during off-peak hours. For zero-downtime, a rolling update strategy involving the load balancer is needed (see [Load Balancer](#load-balancer-nginx) section).**

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

3.  **Navigate and Move JAR (No Rename):**
    ```bash
    cd /data/watermark_service/
    # Optional: Backup or remove older JAR versions
    # mv watermark-service-1.0.0.jar watermark-service-1.0.0.jar.backup
    # Move the new 1.0.1 JAR into the directory:
    mv /tmp/watermark-service-1.0.1.jar ./
    ```

4.  **Restart the service(s) using sudo:**
    *   To restart **all** production instances (8081 & 8082) with the new JAR:
        ```bash
        sudo ./start_watermark_service.sh
        ```
    *   Alternatively, if performing a rolling update (see [Load Balancer](#load-balancer-nginx)):
        ```bash
        # Example: Restart only 8081 after taking it out of LB rotation
        sudo ./start_watermark_service.sh 8081
        # Verify instance 8081... then proceed with 8082
        # sudo ./start_watermark_service.sh 8082
        ```
    *(The script, run as root, must identify `watermark-service-1.0.1.jar` and manage the 8081/8082 instances correctly based on arguments)*

5.  **Verify:** Check logs and service status on ports 8081 and 8082 (see [Verification](#verification)). Check which JAR the running processes are using.
6.  **Exit SSH:** `exit`

## Restarting the Service (Using Sudo)

The `start_watermark_service.sh` script in each respective directory handles stopping the running instance(s) and starting new ones using the **latest versioned JAR** found in the directory. **It must be executed with `sudo`.**

*   **Test (on `v4465a.unx.vstage.co`):**
    ```bash
    ssh <your_username>@v4465a.unx.vstage.co
    cd /data/watermark_service_test/
    sudo ./start_watermark_service.sh
    exit
    ```
    *(Restarts the single instance on port 8089 using the latest JAR)*

*   **Production (on `v4465a.unx.vstage.co`):**
    ```bash
    ssh <your_username>@v4465a.unx.vstage.co
    cd /data/watermark_service/
    ```
    *   To restart **all** production instances (8081 & 8082) using the latest JAR:
        ```bash
        sudo ./start_watermark_service.sh
        ```
    *   To restart **only** the instance on port **8081** using the latest JAR:
        ```bash
        sudo ./start_watermark_service.sh 8081
        ```
    *   To restart **only** the instance on port **8082** using the latest JAR:
        ```bash
        sudo ./start_watermark_service.sh 8082
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
    tail -f /var/log/watermark-service/instance_8081_logger.log
    tail -f /var/log/watermark-service/instance_8082_logger.log

    # --- Monitoring Script Logs ---
    tail -f /var/log/monitor_watermark_service.log
    ```

2.  **Check Process Status & JAR Version:**
    ```bash
    # SSH into the server first
    # Check process details, including the full command line showing the JAR file used
    ps aux | grep watermark-service | grep java
    # Check Java processes specifically
    jps -l | grep watermark-service
    ```
    *Verify the process is running with the expected `watermark-service-1.0.1.jar`.*

3.  **Check Health Endpoint (if available):**
    *   **Test:** `curl http://v4465a.unx.vstage.co:8089/actuator/health`
    *   **Production Instance 1:** `curl http://v4465a.unx.vstage.co:8081/actuator/health`
    *   **Production Instance 2:** `curl http://v4465a.unx.vstage.co:8082/actuator/health`
    *   **Load Balanced Endpoint:** `curl http://<nginx_load_balancer_address_or_hostname>/actuator/health`

    Look for status "UP". Check `/actuator/info` (if available) for version `1.0.1`.

## Configuration

*   Environment-specific configurations are managed *outside* the JAR.
*   The `start_watermark_service.sh` scripts (run via `sudo`) are responsible for finding the latest JAR and passing the correct configuration (ports, profiles, log locations etc.) to the `java -jar` command for each instance.
*   Review the scripts and associated config files (`application-*.properties`, etc.) in `/data/watermark_service_test/` and `/data/watermark_service/`.
*   External dependencies like **HashiCorp Vault** may be used.

## Scheduled Tasks / Cron Jobs

Several automated tasks run on `v4465a.unx.vstage.co` via cron. Key tasks include:

*   **Watermark Service Monitoring (Production):** (`*/3 * * * *`) `/data/watermark_service/monitor_watermark_service.sh` -> `/var/log/monitor_watermark_service.log`. (Note: This monitor script likely needs `sudo` or appropriate permissions if it needs to restart the service).
*   **HashiCorp Vault Monitoring:** (`*/1 * * * *`) `/opt/vault_scripts/monitor_hashicorp_vault_service.sh` -> `/var/log/monitor_hashicorp_vault_service.log`
*   **Vault Key Rotation:** (`0 2 1 * *`) `/opt/vault_scripts/rotate_rsa_key_pair_password_in_hashicorp_vault_service.sh` -> `/var/log/monitor_hashicorp_vault_service.log`
*   **Log Rotation/Cleanup:** (`0 0 1 */3 *`) `/opt/scripts/log_rotate_and_cleanup.sh`
*   **Log Permissions for Monitoring:** (`0 * * * *`) `/usr/local/bin/log_permission.sh` (Ensures Promtail/Loki can read logs, important if service runs as root or specific user).
*   **Grafana Monitoring:** (`* * * * *`) `pgrep grafana || systemctl start grafana-server`

**Note:** The Watermark service monitor (`monitor_watermark_service.sh`) interacts with the main start script or process management. Ensure its logic is compatible with `sudo` requirements and versioned JARs.

## Load Balancer (Nginx)

*   Nginx load balances traffic across production instances (8081, 8082) on `v4465a.unx.vstage.co`.
*   Configuration: `/etc/nginx/nginx.conf`.
*   **Rolling Updates:** Follow the procedure (remove instance from LB, `sudo ./start_watermark_service.sh <port>`, verify, add back, repeat) using `sudo nginx -s reload`.

## Rollback Strategy (Basic)

If deployment of version `1.0.1` fails:

1.  **Identify failure:** Check logs (`/var/log/watermark-service*/instance_*.log`) and health endpoints.
2.  **Stop failed instance(s):** Use `sudo ./start_watermark_service.sh <port>` to attempt restart (it might fail again) or find PID (`ps aux | grep watermark-service`) and use `sudo kill <PID>`. Disable monitoring cron (`sudo crontab -e`) temporarily.
3.  **Remove the faulty JAR:**
    On `v4465a.unx.vstage.co`, in the affected directory (`/data/watermark_service_test/` or `/data/watermark_service/`):
    ```bash
    # Example: Remove the faulty 1.0.1 JAR
    rm ./watermark-service-1.0.1.jar
    ```
    *Ensure the previous version's JAR (e.g., `watermark-service-1.0.0.jar`) is still present in the directory.*
4.  **Restart the service with the previous JAR:**
    ```bash
    # The script MUST now find and run the previous latest JAR (e.g., 1.0.0)
    sudo ./start_watermark_service.sh # Or sudo ./start_watermark_service.sh <port> for prod
    ```
5.  **Verify** the service is functional on the previous version using logs, process checks (`ps aux`), and health endpoints. Check the JAR version being used by the running process.
6.  Re-enable the monitoring cron job if disabled.

---

**Note:** Replace placeholders like `<your_username>`. The functionality heavily relies on the `start_watermark_service.sh` script correctly identifying the latest JAR, handling `sudo` execution, and managing instance ports/configs. **Thoroughly test the script's behavior, especially regarding JAR detection during deployments and rollbacks.**
