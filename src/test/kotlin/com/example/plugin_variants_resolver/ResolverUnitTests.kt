package com.example.plugin_variants_resolver

import com.example.plugin_variants_resolver.model.*
import com.example.plugin_variants_resolver.service.ResolverService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResolverUnitTests {

    private val resolver = ResolverService()

    @Test
    fun `newer release generic beats older prerelease exact`() {
        val plugin = Plugin(
            id = "wsl",
            name = "WSL",
            versions = mutableListOf(
                // older prerelease with exact match
                PluginVersion("wsl", "1.1.0-beta", variants = listOf(
                    Variant(OS.WINDOWS, Arch.ARM64, "http://cdn/wsl-1.1.0-beta-win-arm64.zip")
                ), release = false),
                // newer release with only generic
                PluginVersion("wsl", "1.0.1", variants = listOf(
                    Variant(null, null, "http://cdn/wsl-1.0.1-generic.zip")
                ), release = true)
            )
        )

        val r = resolver.resolve(plugin, ResolverService.Client(OS.WINDOWS, Arch.ARM64))
        assertEquals("1.0.1", r.chosenVersion!!.version)
        assertEquals("http://cdn/wsl-1.0.1-generic.zip", r.chosenVariant!!.url)
        assertEquals(ResolverService.MatchLevel.GENERIC, r.matchLevel)
    }

    @Test
    fun `within same version prefer exact over generic`() {
        val plugin = Plugin(
            id = "tool",
            name = "Tool",
            versions = mutableListOf(
                PluginVersion("tool", "2.0.0", variants = listOf(
                    Variant(null, null, "generic"),
                    Variant(OS.WINDOWS, Arch.ARM64, "exact")
                ), release = true)
            )
        )

        val r = resolver.resolve(plugin, ResolverService.Client(OS.WINDOWS, Arch.ARM64))
        assertEquals("2.0.0", r.chosenVersion!!.version)
        assertEquals("exact", r.chosenVariant!!.url)
        assertEquals(ResolverService.MatchLevel.EXACT, r.matchLevel)
    }

    @Test
    fun `fallback to older version if newest has no compatible nor generic`() {
        val plugin = Plugin(
            id = "tool",
            name = "Tool",
            versions = mutableListOf(
                // newest release but only macOS variants; Windows client can't use these
                PluginVersion("tool", "3.0.0", variants = listOf(
                    Variant(OS.MACOS, Arch.X64, "mac-x64"),
                    Variant(OS.MACOS, Arch.ARM64, "mac-arm64")
                ), release = true),
                // older release with generic available
                PluginVersion("tool", "2.1.0", variants = listOf(
                    Variant(null, null, "generic-2.1.0")
                ), release = true)
            )
        )

        val r = resolver.resolve(plugin, ResolverService.Client(OS.WINDOWS, Arch.X64))
        assertEquals("2.1.0", r.chosenVersion!!.version)
        assertEquals("generic-2.1.0", r.chosenVariant!!.url)
        assertEquals(ResolverService.MatchLevel.GENERIC, r.matchLevel)
    }
}