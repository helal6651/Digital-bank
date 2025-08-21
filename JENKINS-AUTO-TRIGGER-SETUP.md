# Jenkins Job Configuration for Auto-Triggered Pipeline

## 1. Create/Update Jenkins Job

### A. Job Type: Pipeline
1. Go to Jenkins Dashboard
2. Click "New Item" or select existing job
3. Job name: `DigitalBankPipeline`
4. Select: **Pipeline**
5. Click: **OK**

### B. General Configuration
```
✅ GitHub project
   Project url: https://github.com/helal6651/Digital-bank/

✅ Build Triggers:
   ☑️ GitHub hook trigger for GITScm polling
   ☑️ Poll SCM: H/2 * * * * (backup polling every 2 minutes)

✅ Pipeline:
   Definition: Pipeline script from SCM
   SCM: Git
   Repository URL: https://github.com/helal6651/Digital-bank.git
   Credentials: (your GitHub credentials)
   Branch: */main
   Script Path: jenkins-pipeline-merged.groovy
```

### C. Advanced Options
```
✅ Do not allow concurrent builds (prevents queue conflicts)
✅ Build timeout: 30 minutes
✅ Discard old builds: Keep 10 builds, 30 days
```

## 2. Jenkins Plugin Requirements

### Required Plugins:
```bash
# Install these plugins in Jenkins:
1. GitHub Integration Plugin
2. GitHub API Plugin  
3. Git Plugin
4. Pipeline Plugin
5. Docker Pipeline Plugin
6. Kubernetes CLI Plugin
```

### Install Plugins:
1. Go to: **Manage Jenkins** → **Manage Plugins**
2. Search and install each plugin
3. Restart Jenkins if required

## 3. GitHub Webhook Setup

### A. Get Jenkins Webhook URL
```
Format: http://YOUR_JENKINS_URL:8080/github-webhook/

Examples:
- Local: http://localhost:8080/github-webhook/
- External IP: http://192.168.1.100:8080/github-webhook/
- ngrok: http://abc123.ngrok.io/github-webhook/
```

### B. Add Webhook in GitHub
1. Go to: https://github.com/helal6651/Digital-bank/settings/hooks
2. Click: **Add webhook**
3. Configure:
   ```
   Payload URL: http://YOUR_JENKINS_URL:8080/github-webhook/
   Content type: application/json
   Secret: (leave empty)
   SSL verification: Disable (for local Jenkins)
   Events: ☑️ Just the push event
   Active: ☑️
   ```
4. Click: **Add webhook**

### C. Test Webhook
1. Make a small commit to your repository
2. Check webhook delivery in GitHub (Recent Deliveries tab)
3. Verify Jenkins job was triggered

## 4. Queue Management Features

### A. Prevents Concurrent Builds
```groovy
options {
    disableConcurrentBuilds()  // Only one build at a time
}
```

### B. Queue Behavior
- **New commit while build running**: Queued for next execution
- **Multiple commits while building**: Only latest commit will build
- **Queue status**: Visible in Jenkins dashboard

### C. Build Information
- Shows commit author, message, and timestamp
- Unique image tags with build number and git commit
- Proper cleanup of old Docker images

## 5. Workflow Example

### Scenario: Multiple Commits During Build
```
Time  Action                          Jenkins Response
----  -----------------------------   ------------------------
10:00 Commit A pushed                 → Build #15 starts
10:05 Commit B pushed (build running) → Queued
10:10 Commit C pushed (build running) → Replaces B in queue
10:15 Build #15 (Commit A) completes  → Build #16 (Commit C) starts
10:30 Build #16 (Commit C) completes  → Queue empty
```

### Result:
- ✅ Commit A: Built and deployed
- ❌ Commit B: Skipped (replaced by C)
- ✅ Commit C: Built and deployed

## 6. Testing the Setup

### A. Test Auto-Trigger
1. Make a small change to any file
2. Commit and push:
   ```bash
   git add .
   git commit -m "Test auto-trigger"
   git push origin main
   ```
3. Check Jenkins dashboard - should see build start automatically

### B. Test Queue Management
1. Make first commit (build starts)
2. Immediately make second commit while first is building
3. Verify second build waits in queue
4. Verify builds complete in order

## 7. Troubleshooting

### Webhook Not Working:
- Check Jenkins is accessible from internet
- Verify webhook URL is correct
- Check Jenkins logs: Manage Jenkins → System Log
- Test with ngrok if behind firewall

### Pipeline Not Triggering:
- Verify webhook delivery in GitHub
- Check Jenkins job configuration
- Ensure GitHub plugin is installed
- Check SCM polling as backup

### Queue Issues:
- Check "Build Queue" in Jenkins dashboard
- Verify concurrent builds are disabled
- Check build executor availability

## 8. Success Indicators

### You'll know it's working when:
✅ Commit to GitHub → Jenkins build starts automatically
✅ Multiple commits → Proper queuing behavior
✅ Build shows commit info (author, message, etc.)
✅ Unique Docker image tags per build
✅ No concurrent builds running
✅ Proper cleanup of resources

**Your automated CI/CD pipeline is now complete! 🎉**
