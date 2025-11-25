package org.quicklybly.dumbmq.common.dto

import java.util.UUID

data class JobInitDto(
    val jobId: UUID,
    val fileUrls: List<String>,
)
