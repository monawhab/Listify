#!/usr/bin/env python3
"""
Static crash analyzer — free alternative to Claude AI.
Uses Claude API when ANTHROPIC_API_KEY is available, static patterns otherwise.
"""
import json, os, re, sys

PATTERNS = [
    {"pattern": r"NullPointerException", "confidence": "medium",
     "root_cause": "Null reference accessed without null check.",
     "fix_description": "Use Kotlin safe-call (?.) or add null check before accessing the object.",
     "commit_message": "fix: add null safety check to prevent NullPointerException"},
    {"pattern": r"IndexOutOfBoundsException", "confidence": "medium",
     "root_cause": "List/array accessed at index that doesn't exist.",
     "fix_description": "Use list.getOrNull(index) or check list.size before access.",
     "commit_message": "fix: guard list access to prevent IndexOutOfBoundsException"},
    {"pattern": r"NetworkOnMainThreadException", "confidence": "high",
     "root_cause": "Network call made on the main thread.",
     "fix_description": "Wrap in viewModelScope.launch { withContext(Dispatchers.IO) { ... } }",
     "commit_message": "fix: move network call off main thread"},
    {"pattern": r"IllegalStateException.*Fragment", "confidence": "high",
     "root_cause": "Fragment transaction after activity state was saved.",
     "fix_description": "Use commitAllowingStateLoss() or check !isStateSaved() first.",
     "commit_message": "fix: use commitAllowingStateLoss for fragment transaction"},
    {"pattern": r"OutOfMemoryError", "confidence": "low",
     "root_cause": "Memory leak or large bitmap loading without proper sizing.",
     "fix_description": "Profile with LeakCanary, ensure images loaded via Coil with size limits.",
     "commit_message": "fix: reduce memory usage to prevent OOM"},
    {"pattern": r"CalledFromWrongThreadException", "confidence": "high",
     "root_cause": "UI modified from a background thread.",
     "fix_description": "Wrap UI update in runOnUiThread {} or use LiveData/StateFlow.",
     "commit_message": "fix: move UI update to main thread"},
]

def analyze(crash_title, stack_trace):
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if api_key:
        try:
            return analyze_with_claude(api_key, crash_title, stack_trace)
        except Exception as e:
            print(f"Claude API failed: {e} — using static analysis", file=sys.stderr)
    return analyze_static(crash_title, stack_trace)

def analyze_with_claude(api_key, crash_title, stack_trace):
    import urllib.request
    prompt = f'Analyze this Android crash and respond ONLY with JSON (no markdown):\nCRASH: {crash_title}\nSTACK TRACE:\n{stack_trace}\n\nJSON format: {{"root_cause":"...","affected_file":"path or empty","fix_description":"...","commit_message":"fix: ...","confidence":"high|medium|low","fixed_code":"snippet or empty"}}'
    req = urllib.request.Request("https://api.anthropic.com/v1/messages",
        data=json.dumps({"model":"claude-sonnet-4-20250514","max_tokens":1000,"messages":[{"role":"user","content":prompt}]}).encode(),
        headers={"Content-Type":"application/json","x-api-key":api_key,"anthropic-version":"2023-06-01"})
    with urllib.request.urlopen(req, timeout=30) as r:
        text = json.loads(r.read())["content"][0]["text"]
        m = re.search(r'\{.*\}', text, re.DOTALL)
        if m: return json.loads(m.group())
    raise ValueError("No JSON in Claude response")

def analyze_static(crash_title, stack_trace):
    combined = f"{crash_title}\n{stack_trace}"
    for p in PATTERNS:
        if re.search(p["pattern"], combined, re.IGNORECASE):
            file_match = re.search(r'at com\.listify\.([\w.]+)\((\w+\.kt):\d+\)', stack_trace)
            affected_file = ""
            if file_match:
                pkg = file_match.group(1).rsplit(".", 1)[0].replace(".", "/")
                affected_file = f"app/src/main/java/com/listify/{pkg}/{file_match.group(2)}"
            return {**p, "affected_file": affected_file, "fixed_code": "// TODO: apply suggested fix above"}
    return {"root_cause": f"Unrecognized: {crash_title}", "affected_file": "",
            "fix_description": "Manual investigation required.", "commit_message": "fix: investigate crash",
            "confidence": "low", "fixed_code": ""}

if __name__ == "__main__":
    title = os.environ.get("CRASH_TITLE", "Unknown crash")
    trace = open("/tmp/stack_trace.txt").read() if os.path.exists("/tmp/stack_trace.txt") else ""
    result = analyze(title, trace)
    with open("/tmp/fix_result.json", "w") as f:
        json.dump(result, f)
    print(f"Root cause: {result['root_cause']}")
    print(f"Confidence: {result['confidence']}")
