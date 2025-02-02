package com.jh.blog.batch.domain

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

// TODO: 당첨된 체험단 별도 관리 테이블 추가
@Entity
@Table(
    name = "blog_review",
    indexes = [
        Index(name = "idx_blog_review_link", columnList = "link"),
        Index(name = "idx_blog_review_title", columnList = "title"),
        Index(name = "idx_blog_review_created_at", columnList = "created_at"),
    ],
)
class BlogReviewEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    val blogType: BlogType,

    @Column(name = "link", nullable = false, unique = true)
    val link: String,

    @Column(name = "title")
    var title: String? = null,

    @Column(name = "content")
    var content: String? = null,

    @Column(name = "sub_region")
    val subRegion: String,

    @Embedded
    var blogReviewDates: BlogReviewDates = BlogReviewDates(),

    @Column(name = "is_selected")
    val selectionStatus: Boolean? = false,

    @Column(name = "current_applicants")
    var currentApplicants: Int? = null,

    @Column(name = "total_applicants")
    var totalApplicants: Int? = null,

    @Column(name = "done")
    var done: Boolean = false,

    @Column(name = "apply")
    var apply: Boolean = false,

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    var updatedAt: LocalDateTime = createdAt,
) {

    fun done() {
        this.done = true
    }

    fun done(
        title: String,
        content: String,
        reviewerStartDate: String,
        reviewerEndDate: String,
        reviewerSelectDate: String,
        reviewRegisterStartDate: String,
        reviewRegisterEndDate: String,
        currentApplicants: Int,
        totalApplicants: Int,
    ) {
        done(
            title = title,
            content = content,
            blogReviewDates = BlogReviewDates(
                reviewerStartDate,
                reviewerEndDate,
                reviewerSelectDate,
                reviewRegisterStartDate,
                reviewRegisterEndDate,
            ),
            currentApplicants = currentApplicants,
            totalApplicants = totalApplicants,
        )
    }

    private fun done(
        title: String,
        content: String,
        blogReviewDates: BlogReviewDates,
        currentApplicants: Int,
        totalApplicants: Int,
    ) {
        this.done = true
        this.title = title
        this.content = content
        this.blogReviewDates = blogReviewDates
        this.currentApplicants = currentApplicants
        this.totalApplicants = totalApplicants
    }

    fun apply() {
        this.done = true
        this.apply = true
    }
}
