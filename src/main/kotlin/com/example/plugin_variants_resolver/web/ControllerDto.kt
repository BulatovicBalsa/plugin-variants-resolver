package com.example.plugin_variants_resolver.web

import com.example.plugin_variants_resolver.model.Arch
import com.example.plugin_variants_resolver.model.OS

data class PutPluginReq(val id: String, val name: String)


data class PutVersionReq(
    val pluginId: String,
    val version: String,
    val release: Boolean = true,
    val variants: List<VariantReq> = emptyList()
)

data class VariantReq(val os: OS?, val arch: Arch?, val url: String)
data class ResolveReq(val os: OS?, val arch: Arch?)

// Responses
data class ResolveResp(
    val pluginId: String,
    val version: String?,
    val variantUrl: String?,
    val matchLevel: String
)