package org.quicklybly.dumbmq.api

import jakarta.validation.Valid
import org.quicklybly.dumbmq.api.dto.JobRequest
import org.quicklybly.dumbmq.api.dto.JobResponse
import org.quicklybly.dumbmq.service.JobService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/jobs")
class JobController(private val jobService: JobService) {

    @PostMapping
    fun createJob(@Valid @RequestBody request: JobRequest): JobResponse {
        return jobService.createJob(request)
    }

    @GetMapping("/{jobId}")
    fun getReport(@PathVariable jobId: UUID) {

    }
}
