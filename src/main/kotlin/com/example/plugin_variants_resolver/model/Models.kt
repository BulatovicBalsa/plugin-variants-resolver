package com.example.plugin_variants_resolver.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Variant(
    val os: OS?,
    val arch: Arch?,
    val url: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PluginVersion(
    val pluginId: String,
    val version: String,
    val variants: List<Variant> = emptyList(),
    val release: Boolean = true,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Plugin(
    val id: String,
    val name: String,
    val versions: MutableList<PluginVersion> = mutableListOf()
)