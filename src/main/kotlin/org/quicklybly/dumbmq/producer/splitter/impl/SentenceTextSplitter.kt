package org.quicklybly.dumbmq.producer.splitter.impl

import org.quicklybly.dumbmq.producer.splitter.TextSplitter
import java.io.BufferedReader

class SentenceTextSplitter(
    reader: BufferedReader,
    private val configuration: Configuration,
) : TextSplitter(reader) {

    /**
     * Configuration for sentence splitting
     * @param maxChunkSize maximum size of chunk in characters (must be > 0)
     * @param sentenceTerminators set of sentence ending characters
     * @param bufferSize size of read buffer for performance optimization
     */
    data class Configuration(
        val maxChunkSize: Int,
        val sentenceTerminators: Set<Char> = setOf('.', '!', '?'),
        val bufferSize: Int = 8192
    ) {
        init {
            require(maxChunkSize > 0) {
                "maxChunkSize must be positive, got: $maxChunkSize"
            }
            require(bufferSize > 0) {
                "bufferSize must be positive, got: $bufferSize"
            }
            require(sentenceTerminators.isNotEmpty()) {
                "sentenceTerminators must not be empty"
            }
        }
    }

    private val textBuffer = StringBuilder()
    private var nextSentence: String? = null
    private var readerExhausted = false
    private val readBuffer = CharArray(configuration.bufferSize)

    override fun hasNext(): Boolean {
        if (nextSentence != null) {
            return true
        }

        if (!readerExhausted || textBuffer.isNotEmpty()) {
            nextSentence = extractNextSentence()
        }

        return nextSentence != null
    }

    override fun next(): String {
        if (nextSentence == null && !hasNext()) {
            throw NoSuchElementException()
        }

        return nextSentence!!.also {
            nextSentence = null
        }
    }

    private fun extractNextSentence(): String? {
        while (!readerExhausted || textBuffer.isNotEmpty()) {
            val sentenceEndIndex = findSentenceEnd()

            if (sentenceEndIndex != -1) {
                val sentence = textBuffer.substring(0, sentenceEndIndex + 1)
                textBuffer.delete(0, sentenceEndIndex + 1)

                trimLeadingWhitespace()

                return sentence.trim().takeIf { it.isNotEmpty() }
            }

            if (textBuffer.length >= configuration.maxChunkSize) {
                return extractMaxChunk()
            }

            if (!readerExhausted) {
                readMoreData()
            } else {
                return if (textBuffer.isNotEmpty()) {
                    val trimmed = textBuffer.toString().trim()
                    textBuffer.clear()
                    trimmed.takeIf { it.isNotEmpty() }
                } else {
                    null
                }
            }
        }

        return null
    }

    /**
     * Находит индекс конца предложения в буфере
     */
    private fun findSentenceEnd(): Int {
        for (i in textBuffer.indices) {
            val char = textBuffer[i]

            if (char in configuration.sentenceTerminators) {
                if (!isLikelyAbbreviation(i)) {
                    return i
                }
            }
        }

        return -1
    }

    private fun isLikelyAbbreviation(terminatorIndex: Int): Boolean {
        if (textBuffer[terminatorIndex] != '.') {
            return false
        }

        if (terminatorIndex > 0 && terminatorIndex < textBuffer.length - 1) {
            val prevChar = textBuffer[terminatorIndex - 1]
            val nextChar = textBuffer[terminatorIndex + 1]

            if (prevChar.isDigit() && nextChar.isDigit()) {
                return true
            }

            if (terminatorIndex >= 2) {
                val prevPrevChar = textBuffer[terminatorIndex - 2]
                if (prevChar.isLetter() &&
                    prevChar.isUpperCase() &&
                    (prevPrevChar.isWhitespace() || terminatorIndex == 2)
                ) {
                    return true
                }
            }
        }

        return false
    }

    private fun extractMaxChunk(): String {
        var cutIndex = configuration.maxChunkSize

        if (cutIndex < textBuffer.length) {
            for (i in cutIndex - 1 downTo maxOf(0, cutIndex - 100)) {
                if (textBuffer[i].isWhitespace()) {
                    cutIndex = i + 1
                    break
                }
            }
        } else {
            cutIndex = textBuffer.length
        }

        val chunk = textBuffer.substring(0, cutIndex)
        textBuffer.delete(0, cutIndex)
        trimLeadingWhitespace()

        return chunk.trim()
    }


    private fun readMoreData() {
        val charsRead = reader.read(readBuffer)

        if (charsRead > 0) {
            textBuffer.appendRange(readBuffer, 0, charsRead)
        } else {
            readerExhausted = true
        }
    }

    private fun trimLeadingWhitespace() {
        while (textBuffer.isNotEmpty() && textBuffer[0].isWhitespace()) {
            textBuffer.deleteCharAt(0)
        }
    }
}
