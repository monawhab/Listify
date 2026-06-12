# SpecKit — BDD Tests Mapped to Jira

SpecKit gives Listify a lightweight Given/When/Then DSL so unit tests read like
the Jira acceptance criteria they verify. This closes the traceability gap:
every Jira AC has a matching executable test.

## Why

Jira stories have acceptance criteria in plain English. Raw assertions don't
show which AC they cover. SpecKit makes the link explicit:

```kotlin
spec("LIS-5: Display product grid with image, name, price, and rating") {
    given("the API returns a list of products") { ... }
    whenever("the product list screen loads") { ... }
    then("the grid shows all products") { assert(...) }
    and("each product has a name, price, and rating") { assert(...) }
}
```

If a step fails, the error names the spec and the exact Given/When/Then line.

## Coverage

| Spec | Jira Ticket |
|---|---|
| Product grid displays products | LIS-5 |
| Search filters the list | LIS-12 |
| Sort by price | LIS-14 |
| Filter by category | LIS-13 |
| Error state on API failure | LIS-8 |

## Running

```bash
./gradlew testDebugUnitTest --tests "com.listify.spec.*"
```

These run as part of the normal CI unit test step — no extra setup.

## Adding a Spec for a New Story

1. Read the acceptance criteria from the Jira ticket
2. Write one `spec("LIS-XX: <ticket summary>") { ... }` block per AC group
3. Map each AC line to a given/whenever/then/and step

The spec name should start with the ticket key so test reports trace back to Jira.
