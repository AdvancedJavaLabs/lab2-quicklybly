package org.quicklybly.dumbmq.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("dumbmq.splitter")
data class SplitterProperties(
    val maxChunkSize: Int,
    val sentenceTerminators: Set<Char> = setOf('.', '!', '?'),
    val bufferSize: Int = 8192,
)