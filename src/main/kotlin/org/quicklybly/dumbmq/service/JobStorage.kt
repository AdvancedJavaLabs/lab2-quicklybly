package org.quicklybly.dumbmq.service

import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class JobStorage {
    private val jobsInProgress = ConcurrentHashMap.newKeySet<UUID>()

    fun addJob(jobId: UUID) {
        jobsInProgress.add(jobId)
    }

    fun removeJob(jobId: UUID) {
        jobsInProgress.remove(jobId)
    }

    fun containsJob(jobId: UUID): Boolean {
        return jobsInProgress.contains(jobId)
    }
}
