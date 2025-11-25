package org.quicklybly.dumbmq.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("dumbmq.worker")
data class WorkerProperties(
    val count: Int,
)
