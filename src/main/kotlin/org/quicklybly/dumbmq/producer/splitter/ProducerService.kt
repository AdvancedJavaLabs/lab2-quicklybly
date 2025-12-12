package org.quicklybly.dumbmq.producer.splitter

import com.rabbitmq.client.Channel
import mu.KotlinLogging
import org.quicklybly.dumbmq.aggregator.Aggregator
import org.quicklybly.dumbmq.common.dto.AggregatorControlFinishedDto
import org.quicklybly.dumbmq.common.dto.JobInitDto
import org.quicklybly.dumbmq.common.dto.SentenceTaskDto
import org.quicklybly.dumbmq.configuration.RabbitMqConstants
import org.quicklybly.dumbmq.producer.splitter.impl.SentenceTextSplitter
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URI
import java.time.Instant
import java.util.UUID

private val logger = KotlinLogging.logger { }

@Component
class ProducerService(
    private val rabbitTemplate: RabbitTemplate,
    private val splitterConfiguration: SentenceTextSplitter.Configuration,
    private val aggregator: Aggregator,
) {

    data class JobState(
        var totalChunks: Long,
    ) {
        val processStartTime: Instant = Instant.now()
    }

    @RabbitListener(queues = [RabbitMqConstants.INIT_QUEUE])
    fun jobInitListener(
        jobInitDto: JobInitDto,
        @Header(AmqpHeaders.CHANNEL) channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
    ) {
        val jobId = jobInitDto.jobId
        val jobState = JobState(0)
        aggregator.createJob(jobId)
        logger.debug { "job $jobId started" }
        try {
            val fileReaders = jobInitDto.fileUrls.map { createBufferedReader(it) }
            var totalChunks = 0L
            fileReaders.forEach { reader ->
                val splitter: TextSplitter = SentenceTextSplitter(reader, splitterConfiguration)

                while (splitter.hasNext()) {
                    val chunk = splitter.next()
                    totalChunks++
                    val chunkDto = SentenceTaskDto(
                        jobId = jobId,
                        sentence = chunk,
                        chunkId = totalChunks - 1,
                    )

                    rabbitTemplate.convertAndSend(
                        RabbitMqConstants.TASK_EXCHANGE,
                        RabbitMqConstants.TASK_ROUTING_KEY,
                        chunkDto,
                    )
                }
            }
            jobState.totalChunks = totalChunks
            publishSuccessToAggregator(jobId, totalChunks)
            channel.basicAck(deliveryTag, false)
        } catch (e: Exception) {
            logger.error(e) { "job $jobId failed" }
            publishAbortToAggregator(jobId)
            channel.basicNack(deliveryTag, false, false)
        }
        logger.debug { "job $jobId finished" }
    }

    private fun publishSuccessToAggregator(jobId: UUID, totalChunks: Long) {
        publishJobFinishedToAggregator(jobId, totalChunks, false)
    }

    private fun publishAbortToAggregator(jobId: UUID) {
        publishJobFinishedToAggregator(jobId, -1, true)
    }

    private fun publishJobFinishedToAggregator(jobId: UUID, totalChunks: Long, abort: Boolean) {
        val event = AggregatorControlFinishedDto(
            jobId = jobId,
            totalChunks = totalChunks,
            abort = abort,
        )

        rabbitTemplate.convertAndSend(
            RabbitMqConstants.AGGREGATOR_CONTROL_EXCHANGE,
            "",
            event,
        )
    }

    private fun createBufferedReader(url: String): BufferedReader {
        val uri = URI(url)
        val stream = when (uri.scheme) {
            "file" -> File(uri).inputStream()
            else -> throw IllegalArgumentException("Unsupported scheme")
        }

        return BufferedReader(InputStreamReader(stream))
    }
}
