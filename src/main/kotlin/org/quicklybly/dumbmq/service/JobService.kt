package org.quicklybly.dumbmq.service

import org.quicklybly.dumbmq.api.dto.JobRequest
import org.quicklybly.dumbmq.api.dto.JobResponse
import org.quicklybly.dumbmq.common.dto.JobInitDto
import org.quicklybly.dumbmq.configuration.RabbitMqConstants
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.File
import java.util.UUID

@Service
class JobService(
    private val storage: JobStorage,
    private val rabbitTemplate: RabbitTemplate,
) {

    private val outputDir = "/Users/ar.r.lysenko/IdeaProjects/personal/lab2-quicklybly/output"

    fun createJob(request: JobRequest): JobResponse {
        val jobId = UUID.randomUUID()
        storage.addJob(jobId)

        try {
            val correlation = CorrelationData(jobId.toString())
            rabbitTemplate.convertAndSend(
                RabbitMqConstants.INIT_EXCHANGE,
                RabbitMqConstants.INIT_ROUTING_KEY,
                JobInitDto(
                    jobId = jobId,
                    fileUrls = request.fileUrls,
                ),
                correlation,
            )
        } catch (e: Exception) {
            storage.removeJob(jobId)
            throw e
        }

        return JobResponse(jobId)
    }

    fun getReport(jobId: UUID): Resource {
        if (storage.containsJob(jobId)) {
            throw IllegalStateException("Job is not finished")
        }

        val reportFilePath = "$outputDir/$jobId.json"

        val file = File(reportFilePath)
        if (!file.exists()) {
            throw IllegalStateException("Report not found")
        }

        return FileSystemResource(file)
    }
}
