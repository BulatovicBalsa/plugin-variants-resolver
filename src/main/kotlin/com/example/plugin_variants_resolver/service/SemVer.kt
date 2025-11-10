package com.example.plugin_variants_resolver.service

object SemVerComparator : Comparator<String> {
    override fun compare(a: String, b: String): Int {
        val (na, prea) = parse(a)
        val (nb, preb) = parse(b)
        val numCmp = na.zip(nb).map { it.first.compareTo(it.second) }.firstOrNull { it != 0 } ?: 0
        if (numCmp != 0) return numCmp
        if (prea == null && preb != null) return 1
        if (prea != null && preb == null) return -1
        return (prea ?: "").compareTo(preb ?: "")
    }

    private fun parse(s: String): Pair<List<Int>, String?> {
        val parts = s.split('-', limit = 2)
        val nums = parts[0].split('.').mapNotNull { it.toIntOrNull() }
        val pre = parts.getOrNull(1)
        return (nums + List(3 - nums.size) { 0 }) to pre
    }
}