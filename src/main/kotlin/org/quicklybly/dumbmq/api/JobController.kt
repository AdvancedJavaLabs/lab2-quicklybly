package org.quicklybly.dumbmq.api

import jakarta.validation.Valid
import org.quicklybly.dumbmq.api.dto.JobRequest
import org.quicklybly.dumbmq.api.dto.JobResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/jobs")
class JobController {

    @PostMapping
    fun createJob(@Valid @RequestBody request: JobRequest): JobResponse {
        return JobResponse(UUID.randomUUID())
    }
}
