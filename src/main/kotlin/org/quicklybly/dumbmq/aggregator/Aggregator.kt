package org.quicklybly.dumbmq.aggregator

import com.rabbitmq.client.Channel
import mu.KotlinLogging
import org.quicklybly.dumbmq.common.dto.AggregatorControlFinishedDto
import org.quicklybly.dumbmq.common.dto.MetricsDto
import org.quicklybly.dumbmq.common.dto.SentimentScore
import org.quicklybly.dumbmq.common.dto.SortedSentenceHolder
import org.quicklybly.dumbmq.common.dto.WorkerMetricsDto
import org.quicklybly.dumbmq.configuration.RabbitMqConstants
import org.quicklybly.dumbmq.configuration.properties.MetricsProperties
import org.quicklybly.dumbmq.sink.Sink
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import java.util.TreeSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger { }

@Component
class Aggregator(private val sink: Sink, private val metrics: MetricsProperties) {

    private val storage = ConcurrentHashMap<UUID, MetricAggregationDto>()

    data class AnonymizedTextHolder(
        val text: String,
        val chunkId: Long,
    ) : Comparable<AnonymizedTextHolder> {

        override fun compareTo(other: AnonymizedTextHolder): Int {
            return this.chunkId.compareTo(other.chunkId)
        }
    }

    data class MetricAggregationDto(
        var processedChunks: Long = 0,
        var totalChunks: Long? = null,
        var wordCount: Long = 0,
        val topNWords: MutableMap<String, Int> = hashMapOf(),
        var sentimentScore: SentimentScore = SentimentScore(0, 0, 0),
        var anonymizedText: TreeSet<AnonymizedTextHolder> = TreeSet(),
        val sortedSentences: TreeSet<SortedSentenceHolder> = TreeSet()
    )

    @RabbitListener(queues = [RabbitMqConstants.AGGREGATOR_TASK_QUEUE])
    fun controlListener(
        workerMetricsDto: WorkerMetricsDto,
        @Header(AmqpHeaders.CHANNEL) channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
    ) {
        val jobId = workerMetricsDto.jobId

        storage.computeIfPresent(jobId) { _, oldValue ->
            oldValue.processedChunks += 1L
            oldValue.wordCount += workerMetricsDto.wordCount
            oldValue.topNWords.merge(workerMetricsDto.topNWords)
            oldValue.sentimentScore = oldValue.sentimentScore.merge(workerMetricsDto.sentiment)
            oldValue.anonymizedText.add(
                AnonymizedTextHolder(workerMetricsDto.anonymizedText, workerMetricsDto.chunkId)
            )
            oldValue.sortedSentences.add(SortedSentenceHolder(workerMetricsDto.chunk))

            if (oldValue.processedChunks == oldValue.totalChunks || -1L == oldValue.totalChunks) {
                prepareAndSink(jobId, oldValue)
                null
            } else {
                oldValue
            }
        }

        channel.basicAck(deliveryTag, false)
    }

    fun createJob(jobId: UUID) {
        storage[jobId] = MetricAggregationDto()
    }

    @RabbitListener(queues = [RabbitMqConstants.AGGREGATOR_CONTROL_QUEUE])
    fun controlListener(
        controlDto: AggregatorControlFinishedDto,
        @Header(AmqpHeaders.CHANNEL) channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
    ) {
        processStopEvent(controlDto)
        channel.basicAck(deliveryTag, false)
    }

    private fun processStopEvent(controlDto: AggregatorControlFinishedDto) {
        logger.debug { "aggregator received finished event for job ${controlDto.jobId}" }
        storage.computeIfPresent(controlDto.jobId) { _, oldValue ->
            oldValue.totalChunks = controlDto.totalChunks

            // abort or all processed
            if (oldValue.processedChunks == oldValue.totalChunks || -1L == oldValue.totalChunks) {
                prepareAndSink(controlDto.jobId, oldValue)
                null
            } else {
                oldValue
            }
        }
    }

    fun prepareAndSink(
        jobId: UUID,
        dto: MetricAggregationDto,
    ) {
        val metricsDto = MetricsDto(
            jobId = jobId,
            wordCount = dto.wordCount,
            topNWords = dto.topNWords.toList()
                .sortedByDescending { it.second }
                .take(metrics.numberOfTopWords)
                .toMap(),
            sentiment = dto.sentimentScore,
            anonymizedText = getAnonymizedText(dto.anonymizedText),
            sortedSentences = dto.sortedSentences.map { it.sentence },
        )

        sink.sink(metricsDto)
    }

    private fun getAnonymizedText(set: TreeSet<AnonymizedTextHolder>): String {
        val sb = StringBuilder()
        set.forEach { sb.append(it.text) }
        return sb.toString()
    }

    fun MutableMap<String, Int>.merge(other: Map<String, Int>) {
        for ((word, count) in other) {
            this[word] = this.getOrDefault(word, 0) + count
        }
    }
}
