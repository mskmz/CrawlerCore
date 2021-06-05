package com.mskmz.crawler.core

import org.openqa.selenium.chrome.ChromeDriver
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService


//在这里定义所有的核心对象
object CoreObj {
    lateinit var exec: ScheduledExecutorService
    lateinit var driver: ChromeDriver

    fun init(
        exec: ScheduledExecutorService = Executors.newScheduledThreadPool(3),
        path: String = "",
        driver: ChromeDriver? = null
    ) {
        this.exec = exec
        driver?.let {
            this.driver = it
        }
        path.let {
            if (it.isNotEmpty()) {
                System.setProperty("webdriver.chrome.driver", it);
                this.driver = ChromeDriver()
            }
        }
    }
}