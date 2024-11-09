package org.example.nodeFilter

import org.example.Graph.Node

class NodeFilter {

    fun filterUnlinked(nodes: MutableList<Node>, edges: MutableList<List<String>>) {
        val linked = mutableSetOf<String>()
        val linksOut = mutableSetOf<String>()

        // Find nodes that are linked to
        for (edge in edges) {
            linked.add(edge[1])
            linksOut.add(edge[0])
        }

        // Remove nodes that are unlinked
        nodes.removeIf { !linked.contains(it.key) && !linksOut.contains(it.key) }

        // Remove edges that are not linked
        edges.removeIf { !linked.contains(it[1]) }
    }

    fun filterLinkedTo(nodes: MutableList<Node>, edges: MutableList<List<String>>) {
        val linked = mutableMapOf<String, Int>()
        val linksOut = mutableMapOf<String, Int>()

        // Find nodes that are linked to
        for (edge in edges) {
            linked[edge[1]] = (linked[edge[1]] ?: 0) + 1
            linksOut[edge[0]] = (linksOut[edge[0]] ?: 0) + 1
        }

        // Filter nodes in place based on conditions
        nodes.removeIf {
            !((linked.contains(it.key) && (linked[it.key]!! > 5000)) ||
                    (linksOut.contains(it.key) && (linksOut[it.key]!! > 5)))
        }

        val remNodes = nodes.map { it.key }.toMutableSet()

        // Remove edges in place that don't meet the criteria
        edges.removeIf { !remNodes.contains(it[1]) || !remNodes.contains(it[0]) }

        // Update node link counter for nodes that are linked
        nodes.forEach { node ->
            linked[node.key]?.let {
                node.linkCounter = it
            }
        }
    }
}
