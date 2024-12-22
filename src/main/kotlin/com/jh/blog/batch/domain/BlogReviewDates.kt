package com.jh.blog.batch.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class BlogReviewDates(
    @Column(name = "reviewer_start_date")
    var reviewerStartDate: String? = null,

    @Column(name = "reviewer_end_date")
    var reviewerEndDate: String? = null,

    @Column(name = "reviewer_select_date")
    var reviewerSelectDate: String? = null,

    @Column(name = "review_register_start_date")
    var reviewRegisterStartDate: String? = null,

    @Column(name = "review_register_end_date")
    var reviewRegisterEndDate: String? = null
)
