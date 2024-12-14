package com.jh.blog.batch.api

import com.jh.blog.batch.application.BlogCrawlingService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/crawling")
class BlogCrawlingControllerV1(
    private val service: BlogCrawlingService
) {

    @PostMapping("/come-visit")
    fun crawlingComeVisitBlog() {
        service.crawlingComeToPlay()
    }
}
