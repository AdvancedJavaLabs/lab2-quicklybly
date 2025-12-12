package org.quicklybly.dumbmq.worker

import com.rabbitmq.client.Channel
import mu.KotlinLogging
import org.quicklybly.dumbmq.common.dto.SentenceTaskDto
import org.quicklybly.dumbmq.common.dto.WorkerMetricsDto
import org.quicklybly.dumbmq.configuration.RabbitMqConstants
import org.quicklybly.dumbmq.service.NlpService
import org.quicklybly.dumbmq.service.TextAnalyzer
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import java.util.UUID

private val logger = KotlinLogging.logger { }

class Worker(
    private val id: UUID,
    private val rabbitTemplate: RabbitTemplate,
    private val textAnalyzer: TextAnalyzer,
    private val nlpService: NlpService,
) {

    @RabbitListener(queues = [RabbitMqConstants.TASKS_QUEUE])
    fun taskListener(
        taskDto: SentenceTaskDto,
        @Header(AmqpHeaders.CHANNEL) channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
    ) {
        val sentence = taskDto.sentence
        val jobId = taskDto.jobId

        logger.debug { "worker $id received sentence $sentence" }

        val metric = WorkerMetricsDto(
            jobId = jobId,
            workerId = id,
            chunkId = taskDto.chunkId,
            wordCount = textAnalyzer.wordCount(sentence).toLong(),
            topNWords = textAnalyzer.topNWords(sentence),
            sentiment = textAnalyzer.sentimentScore(sentence),
            anonymizedText = nlpService.anonymizeNames(sentence),
            chunk = sentence,
        )

        rabbitTemplate.convertAndSend(
            RabbitMqConstants.AGGREGATOR_TASK_EXCHANGE,
            "",
            metric,
        )

        channel.basicAck(deliveryTag, false)
    }
}
