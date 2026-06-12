# BMad Method for Listify

BMad (Breakthrough Method for Agile AI Driven Development) sits **upstream** of the
existing Listify pipeline. It turns a one-line feature idea into a complete,
Jira-ready planning package — closing the loop from idea to production.

## The Complete Flow

```
Feature idea (one line)
       ↓  bmad_plan.py
BMad agents: Analyst → PM → Architect → UX → Scrum
       ↓
Planning package (PRD, architecture, UX, stories)
       ↓  bmad_to_jira.py
Jira epic + stories created in LIS project
       ↓  existing pipeline
branch → code → PR → CI + Maestro → merge → CD → production
       ↓
Crashlytics → auto bug-fix loop
```

## Scale-Adaptive Tracks

BMad auto-detects the right planning depth:

| Track | When | Artifacts |
|---|---|---|
| Quick Flow | bug fixes, small clear features | tech spec → stories |
| BMad Method | products, complex features | PRD + architecture + UX + stories |
| Enterprise | payment, security, compliance | + security review |

## Usage

```bash
# 1. Generate the planning package
ANTHROPIC_API_KEY=sk-ant-xxx python3 .bmad/bmad_plan.py \
  "Add a wishlist where users save products and view them later"

# 2. Review the package
cat /tmp/bmad_plan.json

# 3. Create the Jira tickets
JIRA_API_TOKEN_B64=xxx python3 .bmad/bmad_to_jira.py
```

Without `ANTHROPIC_API_KEY`, BMad uses structured templates (still produces
usable stories, just not AI-tailored).

## Agents

| Agent | Role |
|---|---|
| Analyst | Researches the problem, writes the brief |
| PM | Writes the PRD with epics + acceptance criteria |
| Architect | Technical approach fitting our MVVM + Clean Architecture |
| UX | Screens and user flows |
| Scrum | Breaks the PRD into Jira-ready stories with estimates |

After Scrum produces stories, the existing dev pipeline takes over —
no manual ticket writing required.
