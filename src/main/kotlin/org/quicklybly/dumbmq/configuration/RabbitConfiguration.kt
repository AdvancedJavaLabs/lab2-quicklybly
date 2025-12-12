package org.quicklybly.dumbmq.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.quicklybly.dumbmq.service.JobStorage
import org.springframework.amqp.core.AcknowledgeMode
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

private val logger = KotlinLogging.logger {}

object RabbitMqConstants {
    const val INIT_EXCHANGE = "init.exchange"
    const val TASK_EXCHANGE = "task.exchange"
    const val AGGREGATOR_TASK_EXCHANGE = "aggregator.task.exchange"
    const val AGGREGATOR_CONTROL_EXCHANGE = "aggregator.control.exchange"

    const val INIT_QUEUE = "init.queue"
    const val TASKS_QUEUE = "tasks.queue"
    const val AGGREGATOR_TASK_QUEUE = "aggregator.task.queue"
    const val AGGREGATOR_CONTROL_QUEUE = "aggregator.control.queue"

    const val INIT_ROUTING_KEY = "init"
    const val TASK_ROUTING_KEY = "task"
}

@Configuration
@EnableRabbit
class RabbitConfiguration {

    // Exchanges
    @Bean(RabbitMqConstants.INIT_EXCHANGE)
    fun initExchange(): DirectExchange {
        return DirectExchange(RabbitMqConstants.INIT_EXCHANGE)
    }

    @Bean(RabbitMqConstants.TASK_EXCHANGE)
    fun taskExchange(): DirectExchange {
        return DirectExchange(RabbitMqConstants.TASK_EXCHANGE)
    }

    @Bean(RabbitMqConstants.AGGREGATOR_TASK_EXCHANGE)
    fun aggregatorTaskExchange(): FanoutExchange {
        return FanoutExchange(RabbitMqConstants.AGGREGATOR_TASK_EXCHANGE)
    }

    @Bean(RabbitMqConstants.AGGREGATOR_CONTROL_EXCHANGE)
    fun aggregatorControlExchange(): FanoutExchange {
        return FanoutExchange(RabbitMqConstants.AGGREGATOR_CONTROL_EXCHANGE)
    }

    // Queues
    @Bean(RabbitMqConstants.INIT_QUEUE)
    fun initQueue(): Queue {
        return Queue(RabbitMqConstants.INIT_QUEUE)
    }

    @Bean(RabbitMqConstants.TASKS_QUEUE)
    fun tasksQueue(): Queue {
        return Queue(RabbitMqConstants.TASKS_QUEUE)
    }

    @Bean(RabbitMqConstants.AGGREGATOR_TASK_QUEUE)
    fun aggregatorTaskQueue(): Queue {
        return Queue(RabbitMqConstants.AGGREGATOR_TASK_QUEUE)
    }

    @Bean(RabbitMqConstants.AGGREGATOR_CONTROL_QUEUE)
    fun aggregatorControlQueue(): Queue {
        return Queue(RabbitMqConstants.AGGREGATOR_CONTROL_QUEUE)
    }

    // Bindings
    @Bean
    fun initBinding(
        @Qualifier(RabbitMqConstants.INIT_EXCHANGE) initExchange: DirectExchange,
        @Qualifier(RabbitMqConstants.INIT_QUEUE) initQueue: Queue,
    ): Binding {
        return BindingBuilder
            .bind(initQueue)
            .to(initExchange)
            .with(RabbitMqConstants.INIT_ROUTING_KEY)
    }

    @Bean
    fun taskBinding(
        @Qualifier(RabbitMqConstants.TASK_EXCHANGE) taskExchange: DirectExchange,
        @Qualifier(RabbitMqConstants.TASKS_QUEUE) tasksQueue: Queue,
    ): Binding {
        return BindingBuilder
            .bind(tasksQueue)
            .to(taskExchange)
            .with(RabbitMqConstants.TASK_ROUTING_KEY)
    }

    @Bean
    fun aggregatorTaskBinding(
        @Qualifier(RabbitMqConstants.AGGREGATOR_TASK_EXCHANGE) aggregatorTaskExchange: FanoutExchange,
        @Qualifier(RabbitMqConstants.AGGREGATOR_TASK_QUEUE) aggregatorTaskQueue: Queue,
    ): Binding {
        return BindingBuilder
            .bind(aggregatorTaskQueue)
            .to(aggregatorTaskExchange)
    }

    @Bean
    fun aggregatorControlBinding(
        @Qualifier(RabbitMqConstants.AGGREGATOR_CONTROL_EXCHANGE) aggregatorControlExchange: FanoutExchange,
        @Qualifier(RabbitMqConstants.AGGREGATOR_CONTROL_QUEUE) aggregatorControlQueue: Queue,
    ): Binding {
        return BindingBuilder
            .bind(aggregatorControlQueue)
            .to(aggregatorControlExchange)
    }

    @Bean
    fun messageConverter(objectMapper: ObjectMapper): MessageConverter {
        return Jackson2JsonMessageConverter(objectMapper)
    }

    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        messageConverter: MessageConverter
    ): SimpleRabbitListenerContainerFactory {
        return SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(messageConverter)
            setAcknowledgeMode(AcknowledgeMode.MANUAL)
            setPrefetchCount(1)
            setDefaultRequeueRejected(false)
        }
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: MessageConverter,
        jobStorage: JobStorage,
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
            setMandatory(true)

            setConfirmCallback { correlationData, ack, cause ->
                if (!ack) {
                    logger.error { "Message not delivered: $cause" }

                    if (correlationData != null) {
                        val jobId = correlationData.id
                        jobStorage.removeJob(UUID.fromString(jobId))
                    }
                }
            }

            setReturnsCallback { returned ->
                logger.error { "Message returned: ${returned.message}" }
            }
        }
    }
}
