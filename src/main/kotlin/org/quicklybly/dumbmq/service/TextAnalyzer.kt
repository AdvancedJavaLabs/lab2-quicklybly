package org.quicklybly.dumbmq.service

import org.quicklybly.dumbmq.common.dto.SentimentScore
import org.quicklybly.dumbmq.configuration.properties.MetricsProperties
import org.springframework.stereotype.Service

@Service
class TextAnalyzer(private val metricsProperties: MetricsProperties) {

    companion object {
        private val spaceRegex = Regex("\\s+")
        private val notWordSymbolRegex = Regex("[^a-zA-Zа-яА-Я0-9]")

        private val positiveWords: Set<String> = setOf(
            "good",
            "great",
            "excellent",
            "wonderful",
            "positive",
            "fantastic",
            "amazing",
            "awesome",
            "love",
            "best",
            "happy",
            "satisfied",
            "enjoy",
            "superb",
            "brilliant"
        )
        private val negativeWords: Set<String> = setOf(
            "bad",
            "terrible",
            "awful",
            "poor",
            "negative",
            "hate",
            "worst",
            "disappointing",
            "frustrating",
            "useless",
            "pathetic",
            "unhappy",
            "regret",
            "miserable",
            "annoyed"
        )
    }

    fun wordCount(text: String): Int {
        return getWords(text).size
    }

    fun topNWords(text: String): Map<String, Int> {
        val words = getWords(text)
        val wordCount = words.groupingBy { it }.eachCount()
        return wordCount.toList()
            .sortedByDescending { it.second }
            .take(metricsProperties.numberOfTopWords)
            .toMap()
    }

    fun sentimentScore(text: String): SentimentScore {
        val words = getWords(text)

        val positiveCount = words.count { positiveWords.contains(it) }
        val negativeCount = words.count { negativeWords.contains(it) }

        val total = positiveCount + negativeCount

        return SentimentScore(
            total = total,
            positive = positiveCount,
            negative = negativeCount,
        )
    }

    private fun getWords(text: String): List<String> {
        return text.split(spaceRegex)
            .map { it.replace(notWordSymbolRegex, "") }
            .filter { it.isNotBlank() }
            .map { it.lowercase() }
    }
}
