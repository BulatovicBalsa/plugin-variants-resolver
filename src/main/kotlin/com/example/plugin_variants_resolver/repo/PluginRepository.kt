package com.example.plugin_variants_resolver.repo

import com.example.plugin_variants_resolver.persistence.PluginEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PluginRepository : JpaRepository<PluginEntity, String> {

    // Resolve needs versions+variants loaded; use fetch join or entity graph:
    @Query(
        """
        select distinct p from PluginEntity p
        left join fetch p.versions v
        left join fetch v.variants
        where p.id = :id
    """
    )
    fun findDeepById(@Param("id") id: String): PluginEntity?
}