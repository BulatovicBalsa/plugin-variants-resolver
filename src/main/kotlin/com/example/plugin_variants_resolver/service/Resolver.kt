package com.example.plugin_variants_resolver.service

import com.example.plugin_variants_resolver.model.*
import org.springframework.stereotype.Service

@Service
class ResolverService {

    data class Client(val os: OS?, val arch: Arch?)

    enum class MatchLevel { EXACT, OS_ONLY, ARCH_ONLY, GENERIC, NONE }

    data class MatchResult(
        val pluginId: String,
        val chosenVersion: PluginVersion?,
        val chosenVariant: Variant?,
        val matchLevel: MatchLevel,
    )

    /**
     * Resolution strategy:
     * 1) Sort versions by: release > prerelease, then SemVer desc.
     * 2) For each version (newest first), pick the best variant:
     *      exact (os+arch) > os-only > arch-only > generic
     * 3) If a version has no variants, it's backward-compatible (generic).
     */
    fun resolve(plugin: Plugin, client: Client): MatchResult {
        val sorted = plugin.versions
            .sortedWith { a, b ->
                val rel = a.release.compareTo(b.release)
                if (rel != 0) return@sortedWith rel // true > false
                SemVerComparator.compare(a.version, b.version)
            }
            .reversed()

        for (ver in sorted) {
            // No variants -> generic
            if (ver.variants.isEmpty()) {
                return MatchResult(plugin.id, ver, null, MatchLevel.GENERIC)
            }

            val best = ver.variants
                .map { it to score(it, client) }
                .filter { it.second.first > 0 } // keep only compatible
                .maxWithOrNull(
                    compareBy<Pair<Variant, Pair<Int, MatchLevel>>> { it.second.first }
                    .thenByDescending { it.second.second }
                )

            if (best != null) {
                val (variant, pair) = best
                val (_, level) = pair
                return MatchResult(plugin.id, ver, variant, level)
            }
        }

        return MatchResult(plugin.id, null, null, MatchLevel.NONE)
    }

    private fun score(v: Variant, c: Client): Pair<Int, MatchLevel> {
        val osMatch = (v.os == null || c.os == null || v.os == c.os)
        val archMatch = (v.arch == null || c.arch == null || v.arch == c.arch)
        return when {
            v.os != null && v.arch != null && osMatch && archMatch -> 4 to MatchLevel.EXACT
            v.os != null && osMatch && v.arch == null              -> 3 to MatchLevel.OS_ONLY
            v.arch != null && archMatch && v.os == null            -> 2 to MatchLevel.ARCH_ONLY
            v.os == null && v.arch == null                         -> 1 to MatchLevel.GENERIC
            else                                                   -> 0 to MatchLevel.NONE
        }
    }
}