package com.example.plugin_variants_resolver.persistence

import com.example.plugin_variants_resolver.model.Arch
import com.example.plugin_variants_resolver.model.OS
import jakarta.persistence.*

@Entity
@Table(name = "variants")
class VariantEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    val os: OS? = null,

    @Enumerated(EnumType.STRING)
    val arch: Arch? = null,

    @Column(nullable = false)
    val url: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_version_id", nullable = false)
    var pluginVersion: PluginVersionEntity
) {
    constructor() : this(url = "", pluginVersion = PluginVersionEntity())
}