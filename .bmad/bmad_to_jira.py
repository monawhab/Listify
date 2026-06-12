#!/usr/bin/env python3
"""
BMad → Jira bridge.

Reads the planning package from bmad_plan.py and creates the epic + stories
in Jira (LIS project). After creation, the existing dev pipeline takes over:
each story can be picked up → branch → code → PR → CI → merge → done.

Usage:
    JIRA_API_TOKEN_B64=xxx python3 .bmad/bmad_to_jira.py
"""
import json, os, sys, urllib.request

JIRA_BASE = "https://androiddevteam.atlassian.net"
JIRA_PROJECT = "LIS"


def create_issue(token_b64, issue_type, summary, description, parent=None, points=None):
    fields = {
        "project": {"key": JIRA_PROJECT},
        "summary": summary,
        "description": {
            "type": "doc", "version": 1,
            "content": [{"type": "paragraph", "content": [{"type": "text", "text": description}]}]
        },
        "issuetype": {"name": issue_type},
    }
    if parent:
        fields["parent"] = {"key": parent}

    payload = json.dumps({"fields": fields}).encode()
    req = urllib.request.Request(
        f"{JIRA_BASE}/rest/api/3/issue",
        data=payload, method="POST",
        headers={"Authorization": f"Basic {token_b64}", "Content-Type": "application/json"}
    )
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())["key"]


def main():
    token_b64 = os.environ.get("JIRA_API_TOKEN_B64")
    if not token_b64:
        print("Set JIRA_API_TOKEN_B64 environment variable")
        sys.exit(1)

    plan = json.load(open("/tmp/bmad_plan.json"))
    brief = plan["brief"]
    stories = plan["stories"]

    # Create an epic for the feature
    epic_summary = f"[BMad] {brief[:80]}"
    epic_key = create_issue(token_b64, "Epic", epic_summary,
                            f"Auto-planned by BMad.\n\nBrief: {brief}\n\nTrack: {plan['track']}")
    print(f"✅ Epic created: {epic_key}")

    # Create stories under the epic
    created = []
    for s in stories:
        desc = s.get("description", "")
        ac = s.get("acceptance_criteria", [])
        if ac:
            desc += "\n\nAcceptance Criteria:\n" + "\n".join(f"- {c}" for c in ac)
        desc += f"\n\nEstimate: {s.get('story_points','?')} points"

        key = create_issue(token_b64, "Story", s.get("summary", "Untitled"), desc, parent=epic_key)
        created.append(key)
        print(f"  ✅ Story: {key} — {s.get('summary','')[:50]}")

    print(f"\n🎉 Created {epic_key} with {len(created)} stories")
    print(f"Board: {JIRA_BASE}/jira/software/projects/{JIRA_PROJECT}/boards/280")
    print("\nThe dev pipeline can now pick up these stories automatically.")


if __name__ == "__main__":
    main()
