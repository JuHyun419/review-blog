package com.jh.blog.batch.domain

import org.springframework.data.jpa.repository.JpaRepository

interface BlogReviewRepository : JpaRepository<BlogReviewEntity, Long> {

    fun existsByLink(link: String): Boolean
}
