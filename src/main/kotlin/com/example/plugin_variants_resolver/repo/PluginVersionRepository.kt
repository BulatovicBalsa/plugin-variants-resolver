package com.example.plugin_variants_resolver.repo

import com.example.plugin_variants_resolver.persistence.PluginVersionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PluginVersionRepository : JpaRepository<PluginVersionEntity, Long> {
    fun findByPlugin_IdAndVersion(pluginId: String, version: String): PluginVersionEntity?
}