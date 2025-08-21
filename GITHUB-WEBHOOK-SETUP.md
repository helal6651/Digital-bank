# GitHub Webhook Configuration Guide

## 1. GitHub Repository Webhook Setup

### Navigate to Repository Settings:
1. Go to: https://github.com/helal6651/Digital-bank
2. Click: **Settings** tab
3. Click: **Webhooks** (left sidebar)
4. Click: **Add webhook**

### Webhook Configuration:
```
Payload URL: http://YOUR_JENKINS_IP:8080/github-webhook/
Content type: application/json
Secret: [Optional - leave empty for now]
SSL verification: Disable SSL verification (for local Jenkins)

Which events would you like to trigger this webhook?
☑️ Just the push event
☐ Send me everything
☐ Let me select individual events

Active: ☑️ (checked)
```

### Example Webhook URL:
- If Jenkins runs on localhost: `http://localhost:8080/github-webhook/`
- If Jenkins has external IP: `http://YOUR_EXTERNAL_IP:8080/github-webhook/`
- For ngrok tunnel: `http://YOUR_NGROK_URL/github-webhook/`

## 2. Test Webhook
After adding webhook:
1. Go to **Recent Deliveries** tab
2. Make a small commit to test
3. Check if webhook shows ✅ successful delivery

## 3. Jenkins Job Configuration
- Pipeline job should be configured to trigger on GitHub webhook
- Use SCM polling or GitHub hook trigger
- Pipeline will auto-run on every commit to main branch

## Troubleshooting:
- Ensure Jenkins is accessible from GitHub (not behind firewall)
- Check Jenkins logs: Manage Jenkins → System Log
- Verify webhook URL is correct and Jenkins is running
