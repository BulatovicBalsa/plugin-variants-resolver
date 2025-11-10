package com.example.plugin_variants_resolver

import com.example.plugin_variants_resolver.model.Arch
import com.example.plugin_variants_resolver.model.OS
import com.example.plugin_variants_resolver.web.PutPluginReq
import com.example.plugin_variants_resolver.web.PutVersionReq
import com.example.plugin_variants_resolver.web.ResolveReq
import com.example.plugin_variants_resolver.web.VariantReq
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebTests(@Autowired val mockMvc: MockMvc) {

    private val mapper = jacksonObjectMapper()
    private fun body(any: Any) = mapper.writeValueAsString(any)

    @Test
    fun `end-to-end resolve uses H2 persistence`() {
        // upsert plugin
        mockMvc.perform(
            put("/api/plugins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(PutPluginReq("wsl", "WSL")))
        ).andExpect(status().isOk)

        // add 1.0.0 win-x64
        mockMvc.perform(
            put("/api/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(
                    PutVersionReq(
                        pluginId = "wsl",
                        version = "1.0.0",
                        release = true,
                        variants = listOf(
                            VariantReq(OS.WINDOWS, Arch.X64, "http://cdn/wsl-1.0.0-win-x64.zip")
                        )
                    )
                ))
        ).andExpect(status().isOk)

        // add 1.0.1 generic (newer, takes precedence for non-specific clients)
        mockMvc.perform(
            put("/api/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(PutVersionReq(
                    pluginId = "wsl",
                    version = "1.0.1",
                    release = true,
                    variants = listOf(
                        VariantReq(null, null, "http://cdn/wsl-1.0.1-generic.zip")
                    )
                )))
        ).andExpect(status().isOk)

        // resolve for Windows/ARM64 (no exact in 1.0.1, so generic 1.0.1 wins)
        mockMvc.perform(
            post("/api/resolve/wsl")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(ResolveReq(OS.WINDOWS, Arch.ARM64)))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.version").value("1.0.1"))
            .andExpect(jsonPath("$.variantUrl").value("http://cdn/wsl-1.0.1-generic.zip"))
            .andExpect(jsonPath("$.matchLevel").value("GENERIC"))
    }
}