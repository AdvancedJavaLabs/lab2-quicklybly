package org.quicklybly.dumbmq.api.dto.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.io.File
import java.net.URI

class ValidFileUrlsValidator : ConstraintValidator<ValidFileUrls, List<String>> {

    companion object {
        private val supportedSchemes = listOf("file")
    }

    override fun initialize(constraintAnnotation: ValidFileUrls) {}

    override fun isValid(
        value: List<String>?,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value == null || value.isEmpty()) return true

        for ((index, url) in value.withIndex()) {
            val result = validateFileUrl(url, context, index)
            if (!result) return false
        }

        return true
    }

    private fun validateFileUrl(
        url: String,
        context: ConstraintValidatorContext,
        index: Int
    ): Boolean {
        return try {
            val uri = URI(url)
            if (uri.scheme !in supportedSchemes) {
                context.addMessage("[$index] Unsupported URL scheme: ${uri.scheme}, use $supportedSchemes")
                return false
            }

            val file = File(uri)
            if (!file.exists()) {
                context.addMessage("[$index] File does not exist: $url")
                return false
            }

            if (!file.canRead()) {
                context.addMessage("[$index] File is not readable: $url")
                return false
            }

            true
        } catch (e: Exception) {
            context.addMessage("[$index] Invalid URL: $url, error: ${e.message}")
            false
        }
    }

    private fun ConstraintValidatorContext.addMessage(message: String) {
        this.disableDefaultConstraintViolation()
        this.buildConstraintViolationWithTemplate(message).addConstraintViolation()
    }
}
