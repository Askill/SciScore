package org.example.IdMapping

data class RefNode(
    val localRefId: Int,
    val open_alex_id: String?,
    val sem_open_alex_id: String?,
    val pubmed_id: String?,
    val doi: String?,
    val pmc_id: String?,
    val arxiv_id: String?,
)
