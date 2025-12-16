package org.quicklybly.dumbmq.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties("dumbmq.metrics")
data class MetricsProperties(
    val numberOfTopWords: Int,
)

@Configuration
@EnableConfigurationProperties(MetricsProperties::class)
class MetricsConfiguration
