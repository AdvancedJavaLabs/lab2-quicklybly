package org.quicklybly.dumbmq.api.dto.validation

import jakarta.validation.Constraint
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidFileUrlsValidator::class])
annotation class ValidFileUrls(
    val message: String = "Each URL must be a valid and readable URL",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Any>> = [],
)
