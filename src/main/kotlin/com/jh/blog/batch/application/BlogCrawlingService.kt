package com.jh.blog.batch.application

import com.jh.blog.batch.common.Const.GS_EXCLUDE_KEYWORDS
import com.jh.blog.batch.common.Const.GS_URL
import com.jh.blog.batch.common.Const.OVER_50000_WON
import com.jh.blog.batch.common.Const.OVER_70000_WON
import com.jh.blog.batch.domain.BlogReviewEntity
import com.jh.blog.batch.domain.BlogReviewRepository
import com.jh.blog.batch.domain.BlogType
import io.github.bonigarcia.wdm.WebDriverManager
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openqa.selenium.Alert
import org.openqa.selenium.By
import org.openqa.selenium.NoAlertPresentException
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.UnhandledAlertException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

// TODO: 크롤링 비즈니스 로직 -> 추상화/구현체 분리
@Service
class BlogCrawlingService(
    private val repository: BlogReviewRepository,
) {

    private val logger = KotlinLogging.logger { }

    @Value("\${blog.my}")
    private lateinit var myBlog: String

    @Value("\${blog.come.id}")
    private lateinit var id: String

    @Value("\${blog.come.password}")
    private lateinit var password: String

    @Transactional
    fun crawlingComeToPlay() {
        init()

        val driver: WebDriver = ChromeDriver(ChromeOptions())

        runCatching {
            webLogin(driver)

            // TODO: 구로/관악/금천, 영등포/동작 크롤링 로직 추가
            crawling(
                driver = driver,
                url = GS_URL,
                excludeKeywords = GS_EXCLUDE_KEYWORDS
            )
        }.onFailure { e ->
            logger.error { "failed crawling -> ${e.message}" }
            e.printStackTrace()
        }.also {
            driver.quit()
        }.onSuccess {
            logger.info { "Crawling Success!" }
        }
    }

    private fun init() {
        System.setProperty("webdriver.chrome.driver", "/Users/juhyun/Desktop/etc/chromedriver-mac-x64/chromedriver")

        WebDriverManager.chromedriver().setup()
    }

    private fun webLogin(driver: WebDriver) {
        driver.get("https://www.cometoplay.kr/login.php")

        driver.findElement(By.id("mb_id")).sendKeys(id)
        driver.findElement(By.id("mb_password")).sendKeys(password)

        driver.findElement(By.cssSelector(".btn_login")).click()
    }

    private fun crawling(driver: WebDriver, url: String, excludeKeywords: List<String>) {
        var currentPage = 1
        driver.get(url + currentPage)

        // TODO: 무한 루프 방지
        while (true) {
            val uniqueBlogLinks = getUniqueBlogLinks(driver)

            for (href in uniqueBlogLinks) {
                if (repository.existsByLink(href)) {
                    continue
                }

                // TODO: 코드 개선
                try {
                    driver.get(href)
                } catch (e: UnhandledAlertException) {

                    // 알림 창 대기 및 처리
                    val alert: Alert? = try {
                        WebDriverWait(driver, Duration.ofSeconds(2))
                            .until(ExpectedConditions.alertIsPresent())
                    } catch (ex: TimeoutException) {
                        logger.info { "알림이 나타나지 않아 Pass 합니다." }
                        null
                    }

                    alert?.accept()
                }

                Thread.sleep(200L)

                val blogReviewEntity = BlogReviewEntity(blogType = BlogType.COME_VISIT, link = href)

                repository.save(blogReviewEntity)

                val title =
                    driver.findElement(By.xpath("//div[contains(@class, 'itname') and contains(@class, 'fotm')]")).text
                val content =
                    driver.findElement(By.xpath("//span[contains(@class, 'itdes') and contains(@class, 'fotr')]")).text

                // TODO: review ~ End 관련 하나의 클래스로 관리 & Aggregate
                val reviewWrap: WebElement = driver.findElement(By.cssSelector(".review_wrap"))

                Thread.sleep(200L)

                // TODO: Coroutines 적용
                // 리뷰어 신청 10.21 ~ 10.27
                val reviewApplyDates = reviewWrap.findElement(By.xpath(".//span[1]")).text

                // 10.21
                val reviewerStartDate = reviewApplyDates.substringAfter("리뷰어 신청").substringBefore("~").trim()

                // 10.27
                val reviewerEndDate = reviewApplyDates.substringAfter("~").trim()

                // 리뷰어 선정일 - 10.28
                val reviewerSelectDate = reviewWrap.findElement(By.xpath(".//span[2]//span")).text.trim()

                // 리뷰등록 10.29 ~ 11.13
                val reviewRegisterDates = reviewWrap.findElement(By.xpath(".//span[3]")).text

                // 10.29
                val reviewRegisterStartDate =
                    reviewRegisterDates.substringAfter("리뷰등록").substringBefore("~").trim()

                // 11.13
                val reviewRegisterEndDate = reviewRegisterDates.substringAfter("~").trim()

                val excludesKeyword = excludeKeywords.any { title.contains(it) || content.contains(it) }
                val provided = driver.findElement(By.xpath("//span[@class='etc2 font_1']")).text

                blogReviewEntity.done(
                    title = title,
                    content = content,
                    reviewerStartDate = reviewerStartDate,
                    reviewerEndDate = reviewerEndDate,
                    reviewerSelectDate = reviewerSelectDate,
                    reviewRegisterStartDate = reviewRegisterStartDate,
                    reviewRegisterEndDate = reviewRegisterEndDate,
                    currentApplicants = 0, // TODO
                    totalApplicants = 0, // TODO
                )

                if (excludesKeyword && !isOver70000WonPrice(provided)) {
                    logger.info { "$title - $content 해당 체험단은 거리 문제로 인해 신청하지 않습니다." }
                    blogReviewEntity.done()
                    repository.save(blogReviewEntity)

                    continue
                }

                if (isUnder50000WonPrice(provided)) {
                    logger.info { "$title 해당 체험단은 5만원 이하의 체험단으로, 신청을 하지 않습니다." }
                    blogReviewEntity.done()
                    repository.save(blogReviewEntity)
                    continue
                }

                driver.findElement(By.cssSelector(".review_req")).click()

                Thread.sleep(100L)

                // TODO: Coroutines 적용
                // 1. 블로그 선택
                val selectElement: WebElement = driver.findElement(By.id("rv_blog"))
                val select = Select(selectElement)
                select.selectByValue(myBlog)

                // 2. 신청자 한마디 입력
                val textArea: WebElement = driver.findElement(By.id("rv_msg"))
                val message = "안녕하세요, 체험단 경험 많은 블로거입니다.\n방문 후 정성스러운 리뷰 보장합니다 :)"
                textArea.sendKeys(message)

                // 3. 안내 내용 확인 후 기재
                val inputField: WebElement = driver.findElement(By.id("rv_memo"))
                inputField.sendKeys("없음")

                // 4. 전체 동의 선택
                val checkbox: WebElement = driver.findElement(By.id("allchk"))
                if (!checkbox.isSelected) {
                    checkbox.click()
                }

                // 5. 리뷰어 신청하기
                val imageButton: WebElement = driver.findElement(By.id("review_btn"))
                imageButton.click()

                Thread.sleep(100L)

                // 6. Alert 창 닫기
                try {
                    val alert: Alert = driver.switchTo().alert()
                    logger.info { "알림 텍스트: ${alert.text}" }
                    alert.accept()
                } catch (e: NoAlertPresentException) {
                    logger.info { "알림 창이 없습니다." }
                } catch (e: UnhandledAlertException) {
                    logger.info { "예상치 못한 알림 창이 나타났습니다." }
                    val alert: Alert = driver.switchTo().alert()
                    alert.accept()
                }

                blogReviewEntity.apply()

                logger.info { "체험단 신청 완료 :: title: $title, link: $href" }

                Thread.sleep(1000L)
            }

            try {
                driver.get(url + currentPage)
                Thread.sleep(100L)

                val nextPageButton = driver.findElement(By.cssSelector(".paging .next"))
                nextPageButton.click()
                currentPage++
                Thread.sleep(2000L)
            } catch (e: NoSuchElementException) {
                logger.info { "next page is not exist! exit crawling ..." }
                return
            }
        }
    }

    private fun getUniqueBlogLinks(driver: WebDriver): MutableSet<String> {
        return driver.findElements(By.cssSelector(".item_box_list a"))
            .mapNotNull { it.getAttribute("href") }
            .filter { !it.startsWith("javascript:") }
            .toMutableSet()
    }
}

fun isOver70000WonPrice(provided: String): Boolean {
    return OVER_70000_WON.any { provided.contains(it) }
}

fun isUnder50000WonPrice(provided: String): Boolean {
    return OVER_50000_WON.none { provided.contains(it) }
}

