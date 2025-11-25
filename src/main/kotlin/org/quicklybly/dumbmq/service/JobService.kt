package org.quicklybly.dumbmq.service

import org.quicklybly.dumbmq.api.dto.JobRequest
import org.quicklybly.dumbmq.api.dto.JobResponse
import org.quicklybly.dumbmq.producer.splitter.ProducerService
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class JobService(private val producerService: ProducerService) {

    private val jobsInProgress = ConcurrentHashMap.newKeySet<UUID>()

    fun createJob(request: JobRequest): JobResponse {
        val jobId = UUID.randomUUID()
        jobsInProgress.add(jobId)
        return JobResponse(jobId)
    }

    fun completeJob(jobId: UUID) {
        jobsInProgress.remove(jobId)
    }

    fun getReport(jobId: UUID) {
        TODO()
    }
}
