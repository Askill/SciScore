package org.example

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import org.example.Graph.Node
import org.example.IdMapping.IdFactory
import java.io.File
import java.io.FileReader
import org.example.ColorFactory.ColorFactory
import org.example.nodeFilter.NodeFilter

fun main() {
    val directoryPath = "x:/unarxive2024/"
    val numberOfFilesToProcess = 5000
    val preProcessor = PreProcessor()
    val preProcessedCache = "preprocessed_input.jsonl"
    //preProcessor.extractRefs(directoryPath, preProcessedCache, numberOfFilesToProcess, "retry.json")

    worker(preProcessedCache)
}

private fun worker(preProcessedCache: String) {
    val gson = Gson()

    val nodes = mutableListOf<Node>()
    val idFactory = IdFactory()
    val edges = mutableListOf<List<String>>()
    val clusters = mutableSetOf<String>()
    val colorFactory = ColorFactory(25)
    val exportClusters = mutableMapOf<String, Map<String, String>>()

    val limit = 1600000
    var counter = 0.0

    // Open the JSONL file and read it line-by-line
    File(preProcessedCache).useLines { lines ->
        lines.forEach { line ->
            if (counter >= limit) return@forEach
            counter += 1

            try {
                // Deserialize each line as a JsonObject
                val jsonObject: JsonObject = gson.fromJson(line, JsonObject::class.java)
                processJsonObject(jsonObject, nodes, edges, clusters, exportClusters, idFactory, colorFactory)

                // Output progress every 1000 lines processed
                if (counter.toInt() % 1000 == 0) {
                    println("Progress: ${(counter / limit) * 100}%")
                }
            } catch (e: Exception) {
                println("Error processing line: ${e.message}")
                // Skip any malformed JSON entries
            }
        }
    }

    println("Finished Graph Building")
    println("Starting Graph Filtering")

    // Apply node filtering
    val nodeFilter = NodeFilter()
    nodeFilter.filterLinkedTo(nodes, edges)
    nodeFilter.filterUnlinked(nodes, edges)
    nodeFilter.filterLinkedTo(nodes, edges)

    println("Starting Save")

    // Prepare final export data
    val export = mutableMapOf<String, Any>().apply {
        put("nodes", nodes)
        put("edges", edges)
        put("clusters", exportClusters.values.toList())
        put("tags", listOf<String>())
    }

    exportAsJson(export, "dataset.json")
}

// Processing a single JsonObject
private fun processJsonObject(
    jsonObject: JsonObject,
    nodes: MutableList<Node>,
    edges: MutableList<List<String>>,
    clusters: MutableSet<String>,
    exportClusters: MutableMap<String, Map<String, String>>,
    idFactory: IdFactory,
    colorFactory: ColorFactory
) {
    var docId = idFactory.getId(arxivId = jsonObject.get("paper_id").asString)
    if (docId == null) {
        docId = idFactory.add(arxivId = jsonObject.get("paper_id").asString)
    }

    val cluster = jsonObject.get("discipline").asString
    if (!exportClusters.containsKey(cluster)) {
        exportClusters[cluster] = mapOf(
            "clusterLabel" to cluster,
            "color" to colorFactory.getColor(),
            "key" to colorFactory.counter.toString()
        )
    }

    // Create a new node for the document
    val node = Node(
        label = jsonObject.getAsJsonObject("metadata").get("title").asString,
        tag = "InData",
        URL = "https://arxiv.org/abs/${jsonObject.get("paper_id").asString}",
        key = docId.toString(),
        cluster = exportClusters[cluster]?.get("key").toString()
    )
    nodes.add(node)

    // Process references in "bib_entries"
    val refs = jsonObject.getAsJsonObject("bib_entries")
    refs.keySet().forEach { key ->
        val entryObject = refs.getAsJsonObject(key)

        if (entryObject.has("ids")) {
            val ids = entryObject.getAsJsonObject("ids")
            var refId = idFactory.getId(
                openAlexId = ids.get("open_alex_id")?.asString ?: "",
                semOpenAlexId = ids.get("sem_open_alex_id")?.asString ?: "",
                pubmedId = ids.get("pubmed_id")?.asString ?: "",
                pmcId = ids.get("pmc_id")?.asString ?: "",
                doi = ids.get("doi")?.asString ?: "",
                arxivId = ids.get("arxiv_id")?.asString ?: ""
            )

            if (refId == null) {
                refId = idFactory.add(
                    openAlexId = ids.get("open_alex_id")?.asString ?: "",
                    semOpenAlexId = ids.get("sem_open_alex_id")?.asString ?: "",
                    pubmedId = ids.get("pubmed_id")?.asString ?: "",
                    pmcId = ids.get("pmc_id")?.asString ?: "",
                    doi = ids.get("doi")?.asString ?: "",
                    arxivId = ids.get("arxiv_id")?.asString ?: ""
                )
            }

            val refCluster = entryObject.get("discipline")?.asString ?: cluster
            if (!exportClusters.containsKey(refCluster)) {
                exportClusters[refCluster] = mapOf(
                    "clusterLabel" to refCluster,
                    "color" to colorFactory.getColor(),
                    "key" to colorFactory.counter.toString()
                )
            }

            // Create a new node for the referenced document
            val refNode = Node(
                label = entryObject.get("bib_entry_raw").asString,
                tag = "Referenced",
                URL = "https://arxiv.org/abs/${ids.get("arxiv_id")?.asString}",
                key = refId.toString(),
                cluster = exportClusters[refCluster]?.get("key").toString()
            )
            nodes.add(refNode)
            clusters.add(refCluster)

            // Add an edge between the main document and the reference
            edges.add(listOf(docId.toString(), refId.toString()))
        }
    }
}

