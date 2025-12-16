package org.quicklybly.dumbmq.common.dto

import java.util.UUID

data class AggregatorControlFinishedDto(
    val jobId: UUID,
    val totalChunks: Long,
    val abort: Boolean,
)
