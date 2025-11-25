package org.quicklybly.dumbmq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DumbMqApplication

fun main(args: Array<String>) {
    runApplication<DumbMqApplication>(*args)
}
