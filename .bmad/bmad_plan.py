#!/usr/bin/env python3
"""
BMad Planning Orchestrator for Listify.

Takes a feature brief and generates a complete planning package:
  Brief → PRD → Architecture notes → UX flows → Jira-ready stories

When ANTHROPIC_API_KEY is set, each BMad agent is powered by Claude.
Otherwise, uses structured templates as a fallback.

The output stories are formatted for direct creation in Jira (LIS project),
which then flows into the existing dev pipeline (branch → PR → CI → merge).

Usage:
    python3 .bmad/bmad_plan.py "Add a wishlist feature where users can save products"
"""
import json, os, sys, urllib.request

CLOUD_ID = "99387c43-be0b-4ea8-b014-2221db44813d"
JIRA_PROJECT = "LIS"


def detect_track(brief: str) -> str:
    """Scale-adaptive: pick planning depth from the brief."""
    brief_lower = brief.lower()
    quick_signals = ["fix", "bug", "typo", "tweak", "adjust", "small"]
    enterprise_signals = ["payment", "security", "compliance", "gdpr", "auth", "encryption", "pci"]
    if any(s in brief_lower for s in enterprise_signals):
        return "enterprise"
    if any(s in brief_lower for s in quick_signals) and len(brief.split()) < 15:
        return "quick_flow"
    return "bmad_method"


def run_agent(role: str, prompt: str) -> str:
    """Run a BMad agent. Uses Claude if available, else a template."""
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if api_key:
        try:
            return call_claude(api_key, role, prompt)
        except Exception as e:
            print(f"[{role}] Claude unavailable ({e}), using template", file=sys.stderr)
    return template_fallback(role, prompt)


def call_claude(api_key: str, role: str, prompt: str) -> str:
    system_prompts = {
        "analyst": "You are a product analyst. Research the problem and write a concise project brief.",
        "pm": "You are a product manager. Write a PRD with a goal, epics, and acceptance criteria.",
        "architect": "You are a software architect. Define the technical approach fitting an Android MVVM + Clean Architecture (Hilt, Retrofit, Navigation Component) codebase.",
        "ux": "You are a UX designer. Describe the screens and user flows.",
        "scrum": "You are a scrum master. Break the PRD into user stories. Output ONLY valid JSON: a list of objects with keys 'summary', 'description', 'acceptance_criteria' (list), 'story_points' (int).",
    }
    payload = json.dumps({
        "model": "claude-sonnet-4-20250514",
        "max_tokens": 2000,
        "system": system_prompts.get(role, "You are a helpful agile assistant."),
        "messages": [{"role": "user", "content": prompt}]
    }).encode()
    req = urllib.request.Request(
        "https://api.anthropic.com/v1/messages", data=payload,
        headers={"Content-Type": "application/json", "x-api-key": api_key,
                 "anthropic-version": "2023-06-01"}
    )
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.loads(r.read())["content"][0]["text"]


def template_fallback(role: str, prompt: str) -> str:
    """Structured templates when no API key — still produces usable artifacts."""
    if role == "scrum":
        # Return a starter story structure
        return json.dumps([{
            "summary": f"Implement: {prompt[:60]}",
            "description": f"As a user, I want this feature so that I get value.\n\nDerived from brief: {prompt}",
            "acceptance_criteria": [
                "Feature is accessible from the relevant screen",
                "Happy path works end to end",
                "Error and empty states handled",
                "Unit tests cover the ViewModel logic",
                "Maestro flow added for the main path"
            ],
            "story_points": 5
        }])
    return f"## {role.upper()} OUTPUT\n\n(Template mode — set ANTHROPIC_API_KEY for AI-generated content)\n\nBrief: {prompt}"


def main():
    if len(sys.argv) < 2:
        print("Usage: python3 bmad_plan.py 'feature brief'")
        sys.exit(1)

    brief = sys.argv[1]
    track = detect_track(brief)
    print(f"# BMad Planning — Track: {track}\n")
    print(f"Brief: {brief}\n")

    artifacts = {}

    # Phase 1: Analysis
    print("## Phase 1: Analysis (Analyst agent)")
    artifacts["brief"] = run_agent("analyst", f"Write a brief for this feature: {brief}")
    print(artifacts["brief"][:300] + "...\n")

    if track != "quick_flow":
        # Phase 2: Planning
        print("## Phase 2: Planning (PM agent)")
        artifacts["prd"] = run_agent("pm", f"Write a PRD for: {brief}")
        print(artifacts["prd"][:300] + "...\n")

        # Phase 3: Architecture
        print("## Phase 3: Architecture (Architect agent)")
        artifacts["architecture"] = run_agent("architect", f"Define the technical approach for: {brief}")
        print(artifacts["architecture"][:300] + "...\n")

    # Phase 4: Stories (Scrum agent) — always
    print("## Phase 4: Story Breakdown (Scrum agent)")
    stories_raw = run_agent("scrum", f"Break this into Jira stories: {brief}\n\nContext PRD: {artifacts.get('prd', brief)}")
    try:
        import re
        m = re.search(r'\[.*\]', stories_raw, re.DOTALL)
        stories = json.loads(m.group()) if m else []
    except Exception:
        stories = []

    print(f"Generated {len(stories)} stories\n")

    # Save the full package
    output = {
        "brief": brief,
        "track": track,
        "artifacts": artifacts,
        "stories": stories
    }
    with open("/tmp/bmad_plan.json", "w") as f:
        json.dump(output, f, indent=2)

    print("## Stories ready for Jira:")
    for i, s in enumerate(stories, 1):
        print(f"  {i}. [{s.get('story_points','?')}pt] {s.get('summary','')}")

    print(f"\n✅ Planning package saved to /tmp/bmad_plan.json")
    print("Next: review, then run bmad_to_jira.py to create the tickets")


if __name__ == "__main__":
    main()
