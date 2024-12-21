package com.jh.blog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@SpringBootApplication
class ReviewBlogApplication

fun main(args: Array<String>) {
    runApplication<ReviewBlogApplication>(*args)
}
