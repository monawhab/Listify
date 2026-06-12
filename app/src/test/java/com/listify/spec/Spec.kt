package com.listify.spec

/**
 * Lightweight BDD DSL for Listify — "SpecKit" style.
 *
 * Lets tests read like Jira acceptance criteria:
 *
 *   spec("LIS-5: Product grid") {
 *     given("the API returns products") { ... }
 *     whenever("the screen loads") { ... }
 *     then("the grid shows all products") { assert(...) }
 *   }
 *
 * This bridges the gap between Jira acceptance criteria (natural language)
 * and executable tests (verifiable), giving traceability from ticket to test.
 */

class SpecScope(val name: String) {
    private val steps = mutableListOf<Pair<String, () -> Unit>>()

    fun given(description: String, block: () -> Unit) {
        steps.add("GIVEN $description" to block)
    }

    fun whenever(description: String, block: () -> Unit) {
        steps.add("WHEN $description" to block)
    }

    fun then(description: String, block: () -> Unit) {
        steps.add("THEN $description" to block)
    }

    fun and(description: String, block: () -> Unit) {
        steps.add("AND $description" to block)
    }

    fun run() {
        for ((label, block) in steps) {
            try {
                block()
            } catch (e: AssertionError) {
                throw AssertionError("[$name] $label failed: ${e.message}", e)
            }
        }
    }
}

fun spec(name: String, block: SpecScope.() -> Unit) {
    val scope = SpecScope(name)
    scope.block()
    scope.run()
}
