package com.example.plugin_variants_resolver.service

import com.example.plugin_variants_resolver.model.Plugin
import com.example.plugin_variants_resolver.model.PluginVersion
import com.example.plugin_variants_resolver.model.Variant
import com.example.plugin_variants_resolver.persistence.PluginEntity
import com.example.plugin_variants_resolver.persistence.PluginVersionEntity
import com.example.plugin_variants_resolver.persistence.VariantEntity
import com.example.plugin_variants_resolver.repo.PluginRepository
import com.example.plugin_variants_resolver.repo.PluginVersionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PluginService(
    private val pluginRepo: PluginRepository,
    private val versionRepo: PluginVersionRepository
) {
    @Transactional
    fun upsertPlugin(id: String, name: String): Plugin {
        val entity = pluginRepo.findById(id).orElse(null)
            ?.apply { this.name = name }
            ?: PluginEntity(id = id, name = name)
        val saved = pluginRepo.save(entity)
        return saved.toModel()
    }

    @Transactional
    fun addOrReplaceVersion(pluginId: String, version: String, release: Boolean, variants: List<Variant>): Plugin {
        val plugin = pluginRepo.findById(pluginId).orElseGet { PluginEntity(id = pluginId, name = pluginId) }
        // Replace existing version if present
        versionRepo.findByPlugin_IdAndVersion(pluginId, version)?.let {
            plugin.versions.removeIf { v -> v.id == it.id }
            versionRepo.delete(it)
        }

        val pv = PluginVersionEntity(
            version = version,
            release = release,
            plugin = plugin
        )
        pv.variants = variants.map {
            VariantEntity(
                os = it.os,
                arch = it.arch,
                url = it.url,
                pluginVersion = pv
            )
        }.toMutableSet()

        plugin.versions.add(pv)
        val saved = pluginRepo.save(plugin)
        return saved.toModel()
    }

    @Transactional(readOnly = true)
    fun getDeep(pluginId: String): Plugin? =
        pluginRepo.findDeepById(pluginId)?.toModel()
}

// --- mapping helpers to your existing domain models ---
private fun PluginEntity.toModel(): Plugin =
    Plugin(
        id = this.id,
        name = this.name,
        versions = this.versions.map { it.toModel(this.id) }.toMutableList()
    )

private fun PluginVersionEntity.toModel(pluginId: String): PluginVersion =
    PluginVersion(
        pluginId = pluginId,
        version = this.version,
        release = this.release,
        variants = this.variants.map { it.toModel() }
    )

private fun VariantEntity.toModel(): Variant =
    Variant(os = this.os, arch = this.arch, url = this.url)