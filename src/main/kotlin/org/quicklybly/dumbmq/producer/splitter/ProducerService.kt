package org.quicklybly.dumbmq.producer.splitter

import com.rabbitmq.client.Channel
import mu.KotlinLogging
import org.quicklybly.dumbmq.common.dto.JobInitDto
import org.quicklybly.dumbmq.configuration.RabbitMqConstants
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger { }

@Component
class ProducerService(val rabbitTemplate: RabbitTemplate) {

    // todo jobStorage
    // todo don't forget to update JobService
    // todo create async API

    @RabbitListener(queues = [RabbitMqConstants.INIT_QUEUE])
    fun jobInitListener(
        jobInitDto: JobInitDto,
        @Header(AmqpHeaders.CHANNEL) channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long,
    ) {
        logger.info { jobInitDto }
        channel.basicAck(deliveryTag, false)
    }
}
