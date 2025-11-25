package org.quicklybly.dumbmq.common.dto

data class JobInitDto(
    val jobId: String,
    val fileUrls: List<String>,
)
