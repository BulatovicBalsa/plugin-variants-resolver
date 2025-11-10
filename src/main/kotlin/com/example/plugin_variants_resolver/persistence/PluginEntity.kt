package com.example.plugin_variants_resolver.persistence

import jakarta.persistence.*

@Entity
@Table(name = "plugins")
class PluginEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    var name: String,
) {
    constructor() : this("", "")

    @OneToMany(mappedBy = "plugin", cascade = [CascadeType.ALL], orphanRemoval = true)
    var versions: MutableSet<PluginVersionEntity> = mutableSetOf()
}