package com.jh.blog.batch.application

import com.jh.blog.batch.common.Const.GGG_EXCLUDE_KEYWORD
import com.jh.blog.batch.common.Const.GGG_URL
import io.github.bonigarcia.wdm.WebDriverManager
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// TODO: 크롤링 비즈니스 로직 -> 추상화/구현체 분리
@Service
class BlogCrawlingService(
    private val comeToPlay: CrawlingComeToPlay,
) {

    private val logger = KotlinLogging.logger { }

    @Transactional
    fun crawlingComeToPlay() {
        init()

        val driver: WebDriver = ChromeDriver(ChromeOptions())

        runCatching {
            comeToPlay.crawling(
                driver = driver,
                url = GGG_URL,
                excludeKeywords = GGG_EXCLUDE_KEYWORD,
                "GGG"
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
}
