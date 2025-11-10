package com.example.plugin_variants_resolver.web

import com.example.plugin_variants_resolver.model.Plugin
import com.example.plugin_variants_resolver.model.Variant
import com.example.plugin_variants_resolver.service.PluginService
import com.example.plugin_variants_resolver.service.ResolverService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class Controller(
    private val pluginService: PluginService,
    private val resolver: ResolverService
) {
    @GetMapping("/plugins")
    fun list(): List<Plugin> = listOf() // (optional) implement pagination if you’d like

    @PutMapping("/plugins")
    fun putPlugin(@RequestBody req: PutPluginReq): Plugin =
        pluginService.upsertPlugin(req.id, req.name)

    @PutMapping("/versions")
    fun putVersion(@RequestBody req: PutVersionReq): Plugin =
        pluginService.addOrReplaceVersion(
            pluginId = req.pluginId,
            version = req.version,
            release = req.release,
            variants = req.variants.map { Variant(it.os, it.arch, it.url) }
        )

    @PostMapping("/resolve/{pluginId}")
    fun resolve(
        @PathVariable pluginId: String,
        @RequestBody req: ResolveReq
    ): ResponseEntity<ResolveResp> {
        val plugin = pluginService.getDeep(pluginId) ?: return ResponseEntity.notFound().build()
        val result = resolver.resolve(plugin, ResolverService.Client(req.os, req.arch))
        return ResponseEntity.ok(
            ResolveResp(
                pluginId = pluginId,
                version = result.chosenVersion?.version,
                variantUrl = result.chosenVariant?.url,
                matchLevel = result.matchLevel.name
            )
        )
    }
}