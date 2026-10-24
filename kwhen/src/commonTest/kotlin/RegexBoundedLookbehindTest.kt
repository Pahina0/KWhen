package ap.panini.kwhen

import ap.panini.kwhen.configs.ENConfig
import ap.panini.kwhen.en.ENController
import ap.panini.kwhen.util.between31
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegexBoundedLookbehindTest {

    private fun findUnboundedLookbehinds(pattern: String): List<String> {
        val violations = mutableListOf<String>()
        var i = 0
        while (i < pattern.length) {
            if (pattern[i] == '\\') {
                i += 2
                continue
            }
            if (pattern.startsWith("(?<=", i) || pattern.startsWith("(?<!", i)) {
                val startIdx = i
                var depth = 1
                var j = i + 4
                var inCharClass = false
                while (j < pattern.length && depth > 0) {
                    val c = pattern[j]
                    if (c == '\\') {
                        j += 2
                        continue
                    }
                    if (c == '[' && !inCharClass) {
                        inCharClass = true
                    } else if (c == ']' && inCharClass) {
                        inCharClass = false
                    } else if (!inCharClass) {
                        if (c == '(') depth++
                        else if (c == ')') depth--
                    }
                    j++
                }
                val lookbehindBody = pattern.substring(startIdx + 4, if (depth == 0) j - 1 else j)
                val fullLookbehind = pattern.substring(startIdx, j)
                if (hasUnboundedQuantifier(lookbehindBody)) {
                    violations.add(fullLookbehind)
                }
                i = j
            } else {
                i++
            }
        }
        return violations
    }

    private fun hasUnboundedQuantifier(body: String): Boolean {
        var k = 0
        var inCharClass = false
        while (k < body.length) {
            val c = body[k]
            if (c == '\\') {
                k += 2
                continue
            }
            if (c == '[' && !inCharClass) {
                inCharClass = true
                k++
                continue
            }
            if (c == ']' && inCharClass) {
                inCharClass = false
                k++
                continue
            }
            if (!inCharClass) {
                if (c == '+' || c == '*') {
                    return true
                }
                if (c == '{') {
                    val closeBrace = body.indexOf('}', k)
                    if (closeBrace != -1) {
                        val content = body.substring(k + 1, closeBrace)
                        if (content.matches(Regex("\\d+,\\s*"))) {
                            return true
                        }
                    }
                }
            }
            k++
        }
        return false
    }

    @Test
    fun testDetectorCatchesUnboundedLookbehinds() {
        val unboundedPatterns = listOf(
            "(?<=\\bthe\\s+)second",
            "(?<=\\b(?:half|quarter)\\s+)hour",
            "(?<!\\d+)day",
            "(?<=\\w*)test",
            "(?<=\\s{2,})item"
        )
        for (pattern in unboundedPatterns) {
            val violations = findUnboundedLookbehinds(pattern)
            assertTrue(
                violations.isNotEmpty(),
                "Expected unbounded look-behind to be detected in: $pattern"
            )
        }

        val boundedPatterns = listOf(
            "(?<=\\bthe\\s{1,5})second",
            "(?<=\\b(?:half|quarter)\\s{1,5})hour",
            "(?<!\\d)day",
            "(?<=\\.)(?!\\w)",
            "(?<=[+*])test",
            "(?<=\\s{1,3})item"
        )
        for (pattern in boundedPatterns) {
            val violations = findUnboundedLookbehinds(pattern)
            assertTrue(
                violations.isEmpty(),
                "Expected no violations for bounded pattern: $pattern, but found: $violations"
            )
        }
    }

    @Test
    fun testAllEnglishParsersAndMergersHaveBoundedLookbehinds() {
        val controller = ENController(ENConfig())
        val checkedPatterns = mutableListOf<Pair<String, String>>()

        for (parser in controller.parsers) {
            checkedPatterns.add((parser::class.simpleName ?: "Parser") to parser.pattern.pattern)
        }

        for (merger in controller.mergers) {
            val name = merger::class.simpleName ?: "Merger"
            checkedPatterns.add("$name.prefix" to merger.prefixPattern.pattern)
            checkedPatterns.add("$name.between" to merger.betweenPattern.pattern)
        }

        checkedPatterns.add("between31" to between31.pattern)

        val violations = mutableListOf<String>()
        for ((name, pattern) in checkedPatterns) {
            val badLookbehinds = findUnboundedLookbehinds(pattern)
            for (bad in badLookbehinds) {
                violations.add("$name contains unbounded look-behind '$bad' in pattern: $pattern")
            }
        }

        assertEquals(
            emptyList(),
            violations,
            "Found unbounded look-behinds that would fail on Android ICU:\n" + violations.joinToString("\n")
        )
    }
}
