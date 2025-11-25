package org.quicklybly.dumbmq.configuration

import org.quicklybly.dumbmq.configuration.properties.SplitterProperties
import org.quicklybly.dumbmq.producer.splitter.impl.SentenceTextSplitter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SplitterProperties::class)
class SplitterConfiguration {

    @Bean
    fun splitterConfig(properties: SplitterProperties): SentenceTextSplitter.Configuration {
        return SentenceTextSplitter.Configuration(
            maxChunkSize = properties.maxChunkSize,
            sentenceTerminators = properties.sentenceTerminators,
            bufferSize = properties.bufferSize,
        )
    }
}