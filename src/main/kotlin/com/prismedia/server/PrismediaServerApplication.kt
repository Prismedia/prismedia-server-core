package com.prismedia.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrismediaServerApplication

fun main(args: Array<String>) {
    runApplication<PrismediaServerApplication>(*args)
}
