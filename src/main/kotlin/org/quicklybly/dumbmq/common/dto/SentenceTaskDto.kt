package org.quicklybly.dumbmq.common.dto

import java.util.UUID

data class SentenceTaskDto(
    val jobId: UUID,
    val chunkId: Long,
    val sentence: String,
)
