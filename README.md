# Watermark Service Deployment

This document outlines the steps to build the Watermark Service JAR file locally, deploy it to the server `v4465a.unx.vstage.co` for both Test and Production environments, and manage the service instances using `sudo`. The current version being deployed is **1.0.1**, requiring **Java 17**.

**Key Environment Details:**
*   **Test Environment:** Runs on port `8089` using the Spring Boot `test` profile.
*   **Production Environment:** Runs on ports `8081` & `8082` using the Spring Boot `prod` profile.

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
*   **Apache Maven:** **Version 3.4.1**.
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
*   **Restart Script:** The `start_watermark_service.sh` script must exist in both `/data/watermark_service_test/` and `/data/watermark_service/` directories and be executable. **Crucially, this script must be able to identify and run the latest versioned JAR file (e.g., `watermark-service-1.0.1.jar`) within its directory AND activate the correct Spring Boot profile (`test` or `prod`).**
*   **Tools:** `scp` or `rsync` available on your local machine for transferring the JAR file.

## Building the Application (Locally)

1.  **Ensure Java 17 is active:**
    ```bash
    java -version # Verify it shows Java 17
    # Set JAVA_HOME or use SDK management tools if needed
    ```
2.  **Ensure correct Maven version is active:**
    ```bash
    mvn -version # Verify it shows Maven 3.4.1
    # Use Maven wrapper (mvnw) if available or adjust PATH if needed
    ```
3.  **Check out the source code from CVS:**
    Use your standard CVS client commands to check out the latest version of the Watermark Service source code to your local machine.

4.  **Navigate to the project's root directory:**
    ```bash
    cd /path/to/your/local/watermark-service-project # This is where you checked out the code
    ```
5.  **Clean and build the project using Maven:**
    *(Ensure your project's pom.xml is configured to produce version 1.0.1 and uses Java 17)*
    ```bash
    mvn clean package
    ```
6.  **Locate the JAR file:** The build process should generate the specific JAR file:
    *   `target/watermark-service-1.0.1.jar`

    *Verify this file exists.*

## Deployment to Server `v4465a.unx.vstage.co`

This process involves transferring the newly built `watermark-service-1.0.1.jar` file to the server and then running the restart script **using `sudo`**. The script handles stopping existing instance(s) and starting new one(s) using the newly transferred versioned JAR and activating the correct Spring Boot profile. **The JAR file is NOT renamed.**

**Generic Steps:**

1.  Build the `watermark-service-1.0.1.jar` file locally using Java 17 and Maven 3.4.1.
2.  Transfer the `watermark-service-1.0.1.jar` file to `/tmp/` on `v4465a.unx.vstage.co`.
3.  SSH into `v4465a.unx.vstage.co`.
4.  Navigate to the deployment directory (test or prod).
5.  (Recommended) Backup any *previous* version JAR file if needed for rollback.
6.  Move the new `watermark-service-1.0.1.jar` from `/tmp/` into the deployment directory. **Do not rename it.**
7.  Execute the `start_watermark_service.sh` script using `sudo` to perform the restart. This script must activate the appropriate profile (`test` or `prod`).
8.  Verify the deployment (see [Verification](#verification)).

---

### Test Environment

*   **Server:** `v4465a.unx.vstage.co`
*   **Deployment Directory:** `/data/watermark_service_test/`
*   **Port:** `8089`
*   **Spring Boot Profile:** `test`
*   **Restart Script:** `/data/watermark_service_test/start_watermark_service.sh` (must find latest JAR and activate `test` profile)
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
    # Optional: Backup or remove older JAR versions
    mv /tmp/watermark-service-1.0.1.jar ./
    ```

4.  **Restart the service using sudo:**
    ```bash
    sudo ./start_watermark_service.sh
    ```
    *(This script, run as root, in `/data/watermark_service_test/` must identify `watermark-service-1.0.1.jar`, activate the `test` profile, and restart the instance on port 8089)*

5.  **Verify:** Check logs for `test` profile activation and service status for port 8089 (see [Verification](#verification)).
6.  **Exit SSH:** `exit`

---

### Production Environment

*   **Server:** `v4465a.unx.vstage.co`
*   **Deployment Directory:** `/data/watermark_service/`
*   **Ports:** `8081`, `8082`
*   **Spring Boot Profile:** `prod`
*   **Restart Script:** `/data/watermark_service/start_watermark_service.sh` (must find latest JAR and activate `prod` profile)
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
        ```
    *(The script, run as root, must identify `watermark-service-1.0.1.jar`, activate the `prod` profile, and manage the 8081/8082 instances correctly based on arguments)*

5.  **Verify:** Check logs for `prod` profile activation and service status on ports 8081 and 8082 (see [Verification](#verification)).
6.  **Exit SSH:** `exit`

## Restarting the Service (Using Sudo)

The `start_watermark_service.sh` script in each respective directory handles stopping the running instance(s) and starting new ones using the **latest versioned JAR** found in the directory and activating the correct **Spring Boot profile**. **It must be executed with `sudo`.**

*   **Test (on `v4465a.unx.vstage.co`):**
    ```bash
    ssh <your_username>@v4465a.unx.vstage.co
    cd /data/watermark_service_test/
    sudo ./start_watermark_service.sh
    exit
    ```
    *(Restarts the single instance on port 8089 using the latest JAR with the `test` profile)*

*   **Production (on `v4465a.unx.vstage.co`):**
    ```bash
    ssh <your_username>@v4465a.unx.vstage.co
    cd /data/watermark_service/
    ```
    *   To restart **all** production instances (8081 & 8082) using the latest JAR with the `prod` profile:
        ```bash
        sudo ./start_watermark_service.sh
        ```
    *   To restart **only** the instance on port **8081** using the latest JAR with the `prod` profile:
        ```bash
        sudo ./start_watermark_service.sh 8081
        ```
    *   To restart **only** the instance on port **8082** using the latest JAR with the `prod` profile:
        ```bash
        sudo ./start_watermark_service.sh 8082
        ```
    ```bash
    exit
    ```

## Verification

After restarting the service, verify it's running correctly on `v4465a.unx.vstage.co`:

1.  **Check Logs:** Tail the specific application logs. Look for messages indicating which Spring Boot profile is active (e.g., "The following profiles are active: test" or "The following profiles are active: prod").
    ```bash
    # SSH into the server first: ssh <your_username>@v4465a.unx.vstage.co

    # --- Test Log ---
    tail -f /var/log/watermark-service-test/instance_8089_logger.log | grep -i profile

    # --- Production Logs ---
    tail -f /var/log/watermark-service/instance_8081_logger.log | grep -i profile
    tail -f /var/log/watermark-service/instance_8082_logger.log | grep -i profile

    # --- Monitoring & Rotation Logs ---
    tail -f /var/log/monitor_watermark_service.log
    tail -f /var/log/log_rotation.log
    ```

2.  **Check Process Status & JAR Version:**
    ```bash
    # SSH into the server first
    ps aux | grep watermark-service | grep java
    jps -l | grep watermark-service
    ```
    *Verify the process is running with `watermark-service-1.0.1.jar`.*

3.  **Check Health Endpoint (if available):**
    *   **Test:** `curl http://v4465a.unx.vstage.co:8089/actuator/health`
    *   **Production Instance 1:** `curl http://v4465a.unx.vstage.co:8081/actuator/health`
    *   **Production Instance 2:** `curl http://v4465a.unx.vstage.co:8082/actuator/health`
    *   **Load Balanced Endpoint:** `curl http://<nginx_load_balancer_address_or_hostname>/actuator/health`

    Look for status "UP". Check `/actuator/info` (if available) for version `1.0.1`.

## Configuration

*   Environment-specific configurations are managed *outside* the JAR, often utilizing **Spring Boot profiles**.
*   The `start_watermark_service.sh` scripts (run via `sudo`) are responsible for:
    *   Finding the latest JAR file.
    *   Activating the correct Spring Boot profile (`test` or `prod`), likely via the `spring.profiles.active` property (e.g., passing `-Dspring.profiles.active=test` or `--spring.profiles.active=prod` to the `java -jar` command).
    *   Passing other necessary configurations (ports, log locations, etc.).
*   Profile-specific properties are typically defined in files like `application-test.properties` or `application-prod.properties` located alongside the JAR or in a `config/` subdirectory within the deployment directory.
*   External dependencies like **HashiCorp Vault** may also provide profile-specific configurations.
*   Review the `start_watermark_service.sh` scripts and any `application-*.properties`/`.yml` files in `/data/watermark_service_test/` and `/data/watermark_service/` to understand the exact configuration mechanism.

## Scheduled Tasks / Cron Jobs

Several automated tasks are configured via cron on the server `v4465a.unx.vstage.co` to ensure the health, security, and maintainability of the Watermark Service and related components. These tasks run automatically at predefined intervals. You can inspect the full list and exact timings using the command `crontab -l` when logged into the server as the relevant user.

Here's a breakdown of the key scheduled tasks:

1.  **Watermark Service Monitoring (Production)**
    *   **Schedule:** Every 3 minutes (`*/3 * * * *`)
    *   **Command:** `/data/watermark_service/monitor_watermark_service.sh`
    *   **Purpose:** Acts as a watchdog for the *production* Watermark Service instances (ports 8081, 8082), automatically detecting crashes or unresponsiveness.
    *   **Mechanism:** Checks process status and likely performs basic health checks. Attempts automatic restart (potentially using `sudo ./start_watermark_service.sh`) if an instance is down.
    *   **Relevance:** Critical for production availability. May mask underlying issues if frequent restarts occur. Temporarily disable during manual maintenance.
    *   **Log File:** `/var/log/monitor_watermark_service.log` (Review for automatic restarts).

2.  **HashiCorp Vault Monitoring**
    *   **Schedule:** Every 1 minute (`*/1 * * * *`)
    *   **Command:** `/opt/vault_scripts/monitor_hashicorp_vault_service.sh`
    *   **Purpose:** Ensures the HashiCorp Vault service is operational, which is critical if the Watermark Service depends on it for secrets or configuration.
    *   **Mechanism:** Checks Vault service status and likely attempts restart if down.
    *   **Relevance:** Supports the reliability of services dependent on Vault.
    *   **Log File:** `/var/log/monitor_hashicorp_vault_service.log`

3.  **Vault Key Rotation**
    *   **Schedule:** Monthly, on the 1st day at 2:00 AM (`0 2 1 * *`)
    *   **Command:** `/opt/vault_scripts/rotate_rsa_key_pair_password_in_hashicorp_vault_service.sh`
    *   **Purpose:** Performs scheduled rotation of RSA keys (used for JWT) within Vault as a security best practice.
    *   **Mechanism:** Interacts with Vault API to update keys. Sends email failure alerts to `masudul.haque@valmet.com`.
    *   **Relevance:** Impacts security tokens. The Watermark Service must be able to utilize the newly rotated keys fetched from Vault.
    *   **Log File:** `/var/log/monitor_hashicorp_vault_service.log` (Shared with Vault monitor).

4.  **Log Rotation/Cleanup (`log_rotate_and_cleanup.sh`)**
    *   **Schedule:** Quarterly, on the 1st day of the month at Midnight (`0 0 1 */3 *`)
    *   **Command:** `/opt/scripts/log_rotate_and_cleanup.sh`
    *   **Purpose:** Manages disk space by rotating and archiving various monitoring and application log files, enforcing a long-term retention policy.
    *   **Target Logs:** Explicitly processes files matching `/var/log/monitor_watermark_service.log`, `/var/log/monitor_hashicorp_vault_service.log`, `/var/log/start_watermark_service.log`, `/var/log/watermark-service-test/*.log`, and `/var/log/watermark-service/*.log`. Skips empty or non-existent files matching the patterns.
    *   **Rotation Method:** Employs a safe "copy-and-truncate" strategy. It first copies the current log content (`cp -ap` preserving metadata) to a temporary file, and then truncates the original log file (`: > logfile`) back to zero size *without changing its inode*. This allows running applications to continue writing to the same open file descriptor seamlessly.
    *   **Archiving:** Compresses (`gzip`) the copied content into an archive file. Archives are stored in dated subdirectories (e.g., `/var/log/watermark_service_monitoring_log_backup/YYYY-MM-DD/`).
    *   **Archive Naming:** Compressed files are named using the pattern: `original_filename-YYYY-MM-DD-inode.gz` (e.g., `instance_8081_logger.log-2023-10-27-12345.gz`). Including the inode number helps identify the specific file instance that was rotated.
    *   **Metadata & Safety:** Preserves original file ownership and permissions (`0640`) on the archives. Uses file locking (`flock` with a 5-minute timeout) during the copy/truncate phase to prevent race conditions if the script were ever triggered concurrently on the same file. Performs integrity checks (`gzip -t`) on archives after creation.
    *   **Retention:** Automatically deletes entire daily backup directories (e.g., `/var/log/watermark_service_monitoring_log_backup/YYYY-MM-DD`) that are older than 365 days. It performs an integrity check on the `.gz` files within a directory before deleting it.
    *   **Logging:** Records its own execution progress, successes, and errors to `/var/log/log_rotation.log`. It also maintains an audit trail of created and deleted backup directories in `/var/log/watermark_service_monitoring_log_backup/audit/trail.log`.
    *   **Relevance:** Crucial for managing disk space and ensuring historical logs are archived safely for one year. Understanding the backup location and naming convention is essential for retrieving older logs.

5.  **Log Permissions for Monitoring (`log_permission.sh`)**
    *   **Schedule:** Hourly, at the top of the hour (`0 * * * *`)
    *   **Command:** `/usr/local/bin/log_permission.sh`
    *   **Purpose:** Ensures log files (likely including `/var/log/watermark-service*/`) are readable by the `promtail` agent for log aggregation in Grafana/Loki.
    *   **Mechanism:** Sets appropriate file ownership and read permissions, necessary if the application logs are created by a different user (e.g., `root`) than the one `promtail` runs as.
    *   **Relevance:** Essential for the observability stack; incorrect permissions prevent logs from appearing in Grafana.
    *   **Log File:** No specific log file configured (output redirected to `/dev/null`).

6.  **Grafana Monitoring**
    *   **Schedule:** Every 1 minute (`* * * * *`)
    *   **Command:** `pgrep grafana || systemctl start grafana-server`
    *   **Purpose:** Simple watchdog to ensure the Grafana server process is running.
    *   **Mechanism:** Checks if the `grafana` process exists; if not, attempts to start the `grafana-server` service using `systemctl`.
    *   **Relevance:** Helps maintain the availability of the monitoring dashboard interface.
    *   **Log File:** No specific log file; `systemctl` actions are logged in the system journal (`journalctl -u grafana-server`).

Understanding these automated tasks provides context for the server's behavior, potential automatic recovery actions, security practices, and how logs and monitoring data are managed over time.

## Load Balancer (Nginx)

*   Nginx load balances traffic across production instances (8081, 8082) on `v4465a.unx.vstage.co`.
*   Configuration: `/etc/nginx/nginx.conf`.
*   **Rolling Updates:** Follow the procedure (remove instance from LB, `sudo ./start_watermark_service.sh <port>`, verify, add back, repeat) using `sudo nginx -s reload`.

## Rollback Strategy (Basic)

If deployment of version `1.0.1` fails:

1.  **Identify failure:** Check logs (`/var/log/watermark-service*/instance_*.log` - look for profile activation and errors) and health endpoints.
2.  **Stop failed instance(s):** Use `sudo ./start_watermark_service.sh <port>` or `sudo kill <PID>`. Disable monitoring cron (`sudo crontab -e`) temporarily.
3.  **Remove the faulty JAR:**
    On `v4465a.unx.vstage.co`, in the affected directory (`/data/watermark_service_test/` or `/data/watermark_service/`):
    ```bash
    rm ./watermark-service-1.0.1.jar
    ```
    *Ensure the previous version's JAR (e.g., `watermark-service-1.0.0.jar`) is present.*
4.  **Restart the service with the previous JAR:**
    ```bash
    # The script MUST now find and run the previous latest JAR (e.g., 1.0.0)
    # AND activate the correct profile (test or prod) for that version.
    sudo ./start_watermark_service.sh # Or sudo ./start_watermark_service.sh <port> for prod
    ```
5.  **Verify** the service is functional on the previous version using logs (check profile!), process checks (`ps aux`), and health endpoints.
6.  Re-enable the monitoring cron job if disabled.

---

**Note:** Replace placeholders like `<your_username>`. The functionality heavily relies on the `start_watermark_service.sh` script correctly identifying the latest JAR, handling `sudo` execution, activating the appropriate Spring Boot profile, and managing instance ports/configs. **Thoroughly test the script's behavior.**
