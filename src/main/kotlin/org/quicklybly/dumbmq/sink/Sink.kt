package org.quicklybly.dumbmq.sink

import com.fasterxml.jackson.databind.ObjectMapper
import org.quicklybly.dumbmq.common.dto.MetricsDto
import org.quicklybly.dumbmq.service.JobStorage
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Paths

@Component
class Sink(
    private val objectMapper: ObjectMapper,
    private val jobStorage: JobStorage,
) {

    private val outputDir = "/Users/ar.r.lysenko/IdeaProjects/personal/lab2-quicklybly/output"

    fun sink(metrics: MetricsDto) {
        val jobId = metrics.jobId
        val fileName = "$jobId.json"

        val dir = File(outputDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val filePath = Paths.get(outputDir, fileName).toAbsolutePath().toString()

        objectMapper.writeValue(File(filePath), metrics)

        jobStorage.removeJob(jobId)
    }
}
