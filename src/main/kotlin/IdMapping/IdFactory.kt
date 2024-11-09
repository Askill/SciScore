package org.example.IdMapping

class IdFactory {
    var idCounter = 100;

    val localRefId = mutableMapOf<String, RefNode>();
    val open_alex_id_map = mutableMapOf<String?, Int>();
    val sem_open_alex_id_map = mutableMapOf<String?, Int>();
    val pubmed_id_map = mutableMapOf<String?, Int>();
    val doi_map = mutableMapOf<String?, Int>();
    val pmc_id_map = mutableMapOf<String?, Int>();
    val arxiv_id_map = mutableMapOf<String?, Int>();

    fun getId(
        openAlexId: String? = null,
        semOpenAlexId: String? = null,
        pubmedId: String? = null,
        doi: String? = null,
        pmcId: String? = null,
        arxivId: String? = null
    ): Int? {
        if (open_alex_id_map.contains(arxivId)) {
            return arxiv_id_map[arxivId]!!
        }
        if (open_alex_id_map.contains(openAlexId)) {
            return open_alex_id_map[openAlexId]!!
        }
        if (open_alex_id_map.contains(semOpenAlexId)) {
            return sem_open_alex_id_map[semOpenAlexId]!!
        }
        if (open_alex_id_map.contains(pubmedId)) {
            return pubmed_id_map[pubmedId]!!
        }
        if (open_alex_id_map.contains(doi)) {
            return doi_map[doi]!!
        }
        if (open_alex_id_map.contains(pmcId)) {
            return pmc_id_map[pmcId]!!
        }
        return null
    }

    fun add(
        openAlexId: String? = null,
        semOpenAlexId: String? = null,
        pubmedId: String? = null,
        doi: String? = null,
        pmcId: String? = null,
        arxivId: String? = null
    ): Int {
        idCounter = idCounter + 1
        localRefId.put(
            idCounter.toString(),
            RefNode(
                idCounter,
                openAlexId,
                semOpenAlexId,
                pubmedId,
                doi,
                pmcId,
                arxivId
            )
        )
        if (openAlexId != null && openAlexId != "") {
            open_alex_id_map.put(openAlexId, idCounter)
        }
        if (semOpenAlexId != null && semOpenAlexId != "") {
            sem_open_alex_id_map.put(semOpenAlexId, idCounter)
        }
        if (pubmedId != null && pubmedId != "") {
            pubmed_id_map.put(pubmedId, idCounter)
        }
        if (doi != null && doi != "") {
            doi_map.put(doi, idCounter)
        }
        if (pmcId != null && pmcId != "") {
            pmc_id_map.put(pmcId, idCounter)
        }
        if (arxivId != null && arxivId != "") {
            arxiv_id_map.put(arxivId, idCounter)
        }

        return idCounter
    }
}