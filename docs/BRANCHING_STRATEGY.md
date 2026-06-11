# Listify Branching Strategy

## Branch Structure

```
main          ← production-ready code only
  ↑ PR + review + CI green
develop       ← integration branch, staging deploys here
  ↑ PR + review + CI green
feature/*     ← individual feature branches
autofix/*     ← auto-generated bug fix branches (from Claude AI)
```

## Flow

### Feature Development
```
1. Branch off develop:    git checkout -b feature/LIS-XX-description develop
2. Build your feature
3. Open PR → develop      (CI runs, staging APK built, 1 approval required)
4. Merge to develop       → staging deploy fires automatically
5. QA signs off on staging
6. Open PR → main         (another CI run + staging build for final check)
7. Merge to main          → production deploy fires automatically
```

### Hotfixes (critical production bug)
```
1. Branch off main:       git checkout -b hotfix/LIS-XX-description main
2. Fix the bug
3. Open PR → main         (fast-track review, CI must pass)
4. Merge to main          → production deploy
5. Backport to develop:   git cherry-pick <commit>
```

### Auto-Fix PRs
```
- Branch: autofix/lis-XX-crash-fix
- Always targets main (fast path for production fixes)
- NEVER auto-merged — requires human review regardless of confidence
- CI must pass before merge is allowed
```

## GitHub Actions Triggers

| Workflow | Trigger | Deploys to |
|---|---|---|
| `ci.yml` | PR or push to main/develop | — (tests only) |
| `staging.yml` | Push to develop or PR to main | Firebase staging-testers |
| `cd.yml` | CI passes on main | Firebase internal-testers + GitHub Release |
| `auto_bugfix.yml` | Crashlytics webhook or manual | Opens PR only |

## Firebase App Distribution Groups

| Group | Source | Who gets it |
|---|---|---|
| `staging-testers` | develop branch | QA team, tech leads |
| `internal-testers` | main branch | All team members |

## Required Secrets for Staging

Add `FIREBASE_APP_ID_STAGING` to GitHub secrets:
- Create a second Android app in Firebase Console with package `com.listify.staging`
- Copy its App ID (format: `1:xxx:android:xxx`)
- Store as `FIREBASE_APP_ID_STAGING` secret
