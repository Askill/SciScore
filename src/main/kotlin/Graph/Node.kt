package org.example.Graph

import kotlinx.serialization.Serializable

@Serializable
data class Node(

    val label: String,
    val URL: String,
    val tag: String,

    val key: String,

    var linkCounter: Int = 0,
    val cluster: String,
)
