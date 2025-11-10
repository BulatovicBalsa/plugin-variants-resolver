package com.example.plugin_variants_resolver

import com.example.plugin_variants_resolver.service.SemVerComparator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class SemVerTests {
    @ParameterizedTest
    @MethodSource("numericPrecedenceExamples")
    fun `numeric precedence`(a: String, b: String) {
        // numbers compare numerically, not lexicographically
        assertTrue(SemVerComparator.compare(a, b) > 0)
    }

    @ParameterizedTest
    @MethodSource("releaseVsPrereleaseExamples")
    fun `release versus prerelease`(release: String, prerelease: String) {
        // release > prerelease of the same base
        assertTrue(SemVerComparator.compare(release, prerelease) > 0)
    }

    @ParameterizedTest
    @MethodSource("prereleaseRankingExamples")
    fun `prerelease ranking lexicographic fallback`(higher: String, lower: String) {
        // higher prerelease string wins when same base and both are prerelease
        assertTrue(SemVerComparator.compare(higher, lower) > 0)
    }

    @ParameterizedTest
    @MethodSource("missingComponentsEqualExamples")
    fun `missing components default to zeros`(a: String, b: String) {
        // missing components default to zeros (1.0 == 1.0.0)
        assertEquals(0, SemVerComparator.compare(a, b))
    }

    companion object {
        @JvmStatic
        fun numericPrecedenceExamples(): Stream<Arguments> = Stream.of(
            Arguments.arguments("1.10.0", "1.2.9"),
            Arguments.arguments("2.0.0", "1.10.9"),
            Arguments.arguments("1.0.10", "1.0.2"),
            Arguments.arguments("0.10.0", "0.2.9"),
            Arguments.arguments("3.0", "2.9.9")
        )

        @JvmStatic
        fun releaseVsPrereleaseExamples(): Stream<Arguments> = Stream.of(
            Arguments.arguments("1.0.0", "1.0.0-beta"),
            Arguments.arguments("2.1.3", "2.1.3-alpha"),
            Arguments.arguments("0.0.1", "0.0.1-rc1"),
            Arguments.arguments("1.2.0", "1.2.0-0"),
            Arguments.arguments("10.0.0", "10.0.0-preview")
        )

        @JvmStatic
        fun prereleaseRankingExamples(): Stream<Arguments> = Stream.of(
            Arguments.arguments("1.0.0-rc2", "1.0.0-beta"),
            Arguments.arguments("1.0.0-beta.2", "1.0.0-beta.1"),
            Arguments.arguments("1.0.0-rc", "1.0.0-beta"),
            Arguments.arguments("1.0.0-b", "1.0.0-a")
        )

        @JvmStatic
        fun missingComponentsEqualExamples(): Stream<Arguments> = Stream.of(
            Arguments.arguments("1.0", "1.0.0"),
            Arguments.arguments("1", "1.0.0"),
            Arguments.arguments("2.1", "2.1.0"),
            Arguments.arguments("0.0", "0.0.0"),
            Arguments.arguments("1.0.0", "1.0")
        )
    }
}