package org.quicklybly.dumbmq.worker

import com.rabbitmq.client.Channel
import mu.KotlinLogging
import org.quicklybly.dumbmq.common.dto.SentenceTaskDto
import org.quicklybly.dumbmq.configuration.RabbitMqConstants
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import java.util.UUID

private val logger = KotlinLogging.logger { }

class Worker(private val id: UUID, private val rabbitTemplate: RabbitTemplate) {

    @RabbitListener(queues = [RabbitMqConstants.TASKS_QUEUE])
    fun taskListener(
        taskDto: SentenceTaskDto,
        @Header(AmqpHeaders.CHANNEL) channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
    ) {
        logger.info { "worker $id received sentence ${taskDto.sentence}" }
        channel.basicAck(deliveryTag, false)
    }
}
