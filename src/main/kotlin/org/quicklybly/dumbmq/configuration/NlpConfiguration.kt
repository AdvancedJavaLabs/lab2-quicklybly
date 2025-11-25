package org.quicklybly.dumbmq.configuration

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Properties

@Configuration
class NlpConfiguration {

    @Bean
    fun pipeline(): StanfordCoreNLP {
        val props = Properties().apply {
            setProperty("annotators", "tokenize,ssplit,pos,lemma,ner")
            setProperty("ner.useSUTime", "0")

            setProperty(
                "ner.model",
                "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz"
            )
        }
        return StanfordCoreNLP(props)
    }
}
