package org.quicklybly.dumbmq.common.dto

import java.util.UUID

data class WorkerMetricsDto(
    val jobId: UUID,
    val workerId: UUID,
    val chunkId: Long,
    val wordCount: Long,
    val topNWords: Map<String, Int>,
    val sentiment: SentimentScore,
    val anonymizedText: String,
    val chunk: String,
)

data class MetricsDto(
    val jobId: UUID,
    val wordCount: Long,
    val topNWords: Map<String, Int>,
    val sentiment: SentimentScore,
    val anonymizedText: String,
    val sortedSentences: List<String>,
)

data class SortedSentenceHolder(
    val sentence: String,
): Comparable<SortedSentenceHolder> {
    val length: Int = sentence.length

    override fun compareTo(other: SortedSentenceHolder): Int {
        // Сортировка по длине предложения
        return this.sentence.length - other.sentence.length
    }

    override fun toString(): String = sentence
}

data class SentimentScore(
    val total: Int,
    val positive: Int,
    val negative: Int,
) {
    val score: Double = if (total == 0) {
        0.0
    } else {
        (positive - negative).toDouble() / total
    }

    fun merge(other: SentimentScore): SentimentScore {
        return SentimentScore(
            total = total + other.total,
            positive = positive + other.positive,
            negative = negative + other.negative,
        )
    }
}
