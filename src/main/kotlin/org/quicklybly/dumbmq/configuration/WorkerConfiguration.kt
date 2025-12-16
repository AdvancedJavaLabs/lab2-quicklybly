package org.quicklybly.dumbmq.configuration

import mu.KotlinLogging
import org.quicklybly.dumbmq.configuration.properties.WorkerProperties
import org.quicklybly.dumbmq.worker.Worker
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.util.UUID

private val logger = KotlinLogging.logger { }

@Configuration
@EnableConfigurationProperties(WorkerProperties::class)
class WorkerConfiguration : BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private var workerCount: Int = 0

    override fun setEnvironment(environment: Environment) {
        workerCount = environment.getProperty("dumbmq.worker.count")?.toInt()
            ?: throw IllegalArgumentException("Worker count is not set")
    }

    override fun postProcessBeanDefinitionRegistry(
        registry: BeanDefinitionRegistry,
    ) {
        logger.info { "Registering $workerCount workers" }

        repeat(workerCount) { index ->
            val workerId = UUID.randomUUID()
            val beanName = "worker-$index-$workerId"

            val beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(Worker::class.java)
                .addConstructorArgValue(workerId)
                .addConstructorArgReference("rabbitTemplate")
                .addConstructorArgReference("textAnalyzer")
                .addConstructorArgReference("nlpService")
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .beanDefinition

            registry.registerBeanDefinition(beanName, beanDefinition)
            logger.info { "Registered worker bean: $beanName with ID: $workerId" }
        }
    }
}
