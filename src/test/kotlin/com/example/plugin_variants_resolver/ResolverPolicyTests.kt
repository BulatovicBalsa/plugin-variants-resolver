package com.example.plugin_variants_resolver

import com.example.plugin_variants_resolver.model.*
import com.example.plugin_variants_resolver.service.ResolverService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResolverPolicyTests {

    private val resolver = ResolverService()

    @Test
    fun `higher release with generic beats lower release with exact`() {
        val plugin = Plugin(
            id = "p",
            name = "P",
            versions = mutableListOf(
                PluginVersion("p", "1.2.0", variants = listOf(
                    Variant(null, null, "generic-1.2.0")
                ), release = true),
                PluginVersion("p", "1.1.9", variants = listOf(
                    Variant(OS.WINDOWS, Arch.X64, "exact-1.1.9")
                ), release = true)
            )
        )

        val r = resolver.resolve(plugin, ResolverService.Client(OS.WINDOWS, Arch.X64))
        assertEquals("1.2.0", r.chosenVersion!!.version)
        assertEquals("generic-1.2.0", r.chosenVariant!!.url)
        assertEquals(ResolverService.MatchLevel.GENERIC, r.matchLevel)
    }

    @Test
    fun `release generic beats higher numeric prerelease exact`() {
        val plugin = Plugin(
            id = "p",
            name = "P",
            versions = mutableListOf(
                // higher numeric, but prerelease
                PluginVersion("p", "2.0.0-beta", variants = listOf(
                    Variant(OS.WINDOWS, Arch.ARM64, "exact-2.0.0-beta")
                ), release = false),
                // lower numeric, but release & generic
                PluginVersion("p", "1.9.9", variants = listOf(
                    Variant(null, null, "generic-1.9.9")
                ), release = true)
            )
        )

        val r = resolver.resolve(plugin, ResolverService.Client(OS.WINDOWS, Arch.ARM64))
        assertEquals("1.9.9", r.chosenVersion!!.version)
        assertEquals("generic-1.9.9", r.chosenVariant!!.url)
        assertEquals(ResolverService.MatchLevel.GENERIC, r.matchLevel)
    }

    @Test
    fun `within same version os-only beats arch-only beats generic`() {
        val plugin = Plugin(
            id = "p",
            name = "P",
            versions = mutableListOf(
                PluginVersion("p", "3.0.0", variants = listOf(
                    Variant(null, null, "generic"),
                    Variant(null, Arch.ARM64, "arch-only"),
                    Variant(OS.WINDOWS, null, "os-only")
                ), release = true)
            )
        )

        val r = resolver.resolve(plugin, ResolverService.Client(OS.WINDOWS, Arch.ARM64))
        assertEquals("3.0.0", r.chosenVersion!!.version)
        assertEquals("os-only", r.chosenVariant!!.url)
        assertEquals(ResolverService.MatchLevel.OS_ONLY, r.matchLevel)
    }

    @Test
    fun `no matches anywhere returns NONE`() {
        val plugin = Plugin(
            id = "p",
            name = "P",
            versions = mutableListOf(
                PluginVersion("p", "1.0.0", variants = listOf(
                    Variant(OS.LINUX, Arch.X64, "linux-x64")
                ), release = true),
                PluginVersion("p", "0.9.0", variants = listOf(
                    Variant(OS.MACOS, Arch.ARM64, "mac-arm64")
                ), release = true)
            )
        )

        val r = resolver.resolve(plugin, ResolverService.Client(OS.WINDOWS, Arch.X64))
        assertEquals(null, r.chosenVersion)
        assertEquals(ResolverService.MatchLevel.NONE, r.matchLevel)
    }
}