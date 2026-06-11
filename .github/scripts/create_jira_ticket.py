#!/usr/bin/env python3
import json, urllib.request, os, sys

CLOUD = "99387c43-be0b-4ea8-b014-2221db44813d"
TOKEN_B64 = os.environ["JIRA_API_TOKEN_B64"]
CRASH_TITLE = os.environ.get("CRASH_TITLE", "Unknown crash")
STACK_TRACE = open("/tmp/stack_trace.txt").read() if os.path.exists("/tmp/stack_trace.txt") else "No stack trace"
VERSION = os.environ.get("CRASH_VERSION", "unknown")

body = {
    "type": "doc", "version": 1,
    "content": [{"type": "paragraph", "content": [
        {"type": "text", "text": f"Auto-detected crash from Firebase Crashlytics.\n\nVersion: {VERSION}\n\nStack Trace:\n{STACK_TRACE}"}
    ]}]
}

payload = json.dumps({
    "fields": {
        "project": {"key": "LIS"},
        "summary": f"[AUTO] Crash: {CRASH_TITLE}",
        "description": body,
        "issuetype": {"name": "Bug"},
        "priority": {"name": "High"},
        "labels": ["auto-detected", "crashlytics"]
    }
}).encode()

req = urllib.request.Request(
    f"https://androiddevteam.atlassian.net/rest/api/3/issue",
    data=payload, method="POST",
    headers={
        "Authorization": f"Basic {TOKEN_B64}",
        "Content-Type": "application/json"
    }
)
try:
    with urllib.request.urlopen(req) as r:
        data = json.loads(r.read())
        ticket = data.get("key", "UNKNOWN")
        print(f"ticket={ticket}")
        with open(os.environ.get("GITHUB_OUTPUT", "/tmp/output"), "a") as f:
            f.write(f"ticket={ticket}\n")
except Exception as e:
    print(f"ticket=LIS-AUTO", flush=True)
    print(f"Error: {e}", file=sys.stderr)
