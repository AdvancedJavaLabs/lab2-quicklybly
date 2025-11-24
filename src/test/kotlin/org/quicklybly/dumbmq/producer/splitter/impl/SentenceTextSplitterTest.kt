package org.quicklybly.dumbmq.producer.splitter.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.BufferedReader
import java.io.StringReader

class SentenceTextSplitterTest {

    @Test
    fun `should split simple sentences correctly`() {
        val text = "First sentence. Second one! Third?"
        val reader = BufferedReader(StringReader(text))
        val config = SentenceTextSplitter.Configuration(
            maxChunkSize = 100,
            sentenceTerminators = setOf('.', '!', '?')
        )
        val splitter = SentenceTextSplitter(reader, config)

        assertThat("First sentence.").isEqualTo(splitter.next())
        assertThat("Second one!").isEqualTo(splitter.next())
        assertThat("Third?").isEqualTo(splitter.next())
        assertThat(splitter.hasNext()).isFalse()
    }

    @Test
    fun `should handle maxChunkSize correctly`() {
        val text = "This is a very long sentence without terminators"
        val reader = BufferedReader(StringReader(text))
        val config = SentenceTextSplitter.Configuration(
            maxChunkSize = 20,
            sentenceTerminators = setOf('.')
        )
        val splitter = SentenceTextSplitter(reader, config)

        val chunk = splitter.next()
        assertThat(chunk.length).isLessThanOrEqualTo(20)
        assertThat(splitter.hasNext()).isTrue()
    }

    @Test
    fun `should throw on invalid configuration`() {
        assertThrows<IllegalArgumentException> {
            SentenceTextSplitter.Configuration(
                maxChunkSize = 0,
                sentenceTerminators = setOf('.')
            )
        }
    }
}