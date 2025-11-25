package org.quicklybly.dumbmq.service

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.springframework.stereotype.Service

@Service
class NlpService(private val pipeline: StanfordCoreNLP) {

    companion object {
        private const val NAME_PLACEHOLDER = "[NAME]"
    }

    fun anonymizeNames(text: String): String {
        val document = pipeline.processToCoreDocument(text)
        val entities = document.entityMentions()
            .filter { it.entityType() == "PERSON" }

        var result = text

        for (entity in entities) {
            val name = entity.text()
            val regex = Regex("\\b$name\\b")
            result = regex.replace(result, NAME_PLACEHOLDER)
        }

        return result
    }
}
