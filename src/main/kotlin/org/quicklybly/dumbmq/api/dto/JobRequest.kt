package org.quicklybly.dumbmq.api.dto

import org.quicklybly.dumbmq.api.dto.validation.ValidFileUrls

data class JobRequest(
    @field:ValidFileUrls
    val fileUrls: List<String>,
)
