package com.jh.blog.batch.domain

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class BlogReviewEntityTest : BehaviorSpec() {

    private val blogReviewEntity = BlogReviewEntity(blogType = BlogType.COME_VISIT, link = "")

    init {
        given("done() 함수는") {
            `when`("파라미터가 없을 경우") {
                blogReviewEntity.done()

                then("done 을 true 로 변경한다.") {
                    blogReviewEntity.done shouldBe true
                }
            }

            `when`("파라미터와 함께 호출하면") {
                val expected = "title"
                blogReviewEntity.done(expected, "", "", "", "", "", "", 0, 0)

                then("내부 데이터를 파라미터 값으로 설정한다.") {
                    blogReviewEntity.title shouldBe expected
                }
            }
        }

        given("apply() 함수는") {
            `when`("호출이 되면") {
                blogReviewEntity.apply()

                then("done, apply 를 true 로 설정한다.") {
                    assertSoftly(blogReviewEntity) {
                        done shouldBe true
                        apply shouldBe true
                    }
                }
            }
        }
    }

}
