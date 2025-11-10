package com.example.plugin_variants_resolver.persistence

import jakarta.persistence.*


@Entity
@Table(
    name = "plugin_versions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["plugin_id", "version"])]
)
class PluginVersionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val version: String,

    @Column(nullable = false)
    val release: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_id", nullable = false)
    var plugin: PluginEntity,
) {
    constructor() : this(version = "", plugin = PluginEntity())

    @OneToMany(mappedBy = "pluginVersion", cascade = [CascadeType.ALL], orphanRemoval = true)
    var variants: MutableSet<VariantEntity> = mutableSetOf()
}