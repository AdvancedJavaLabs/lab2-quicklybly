package org.quicklybly.dumbmq.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.quicklybly.dumbmq.configuration.NlpConfiguration

class NlpServiceTest {
    private val config = NlpConfiguration()
    private val service = NlpService(config.pipeline())

    @Test
    fun test() {
        val text = "John met Anna in New York."
        val result = service.anonymizeNames(text)

        assertThat(result).isEqualTo("[NAME] met [NAME] in New York.")
    }
}
