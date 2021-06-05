package com.mskmz.crawler.core

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

object SeleniumUtils {
    fun FindInputForLabel(
        str: String,
        index: Int = 0,
        reverse: Boolean = false,
        webElement: WebElement? = null,
        driver: WebDriver = CoreObj.driver
    ) {
        val labelList = if (webElement == null) {
            driver.findElements(By.ByTagName("label"))
        } else {
            webElement.findElements(By.ByTagName("label"))
        }
        if (reverse) {
            labelList.reversed()
        }
        var i = 0
        labelList.forEach {
            if (it.text.contains(str) && i == index) {
                FindTagNameNeighborForEle(it, "input")
            } else {
                i++
            }
        }
    }

    fun FindTagNameNeighborForEle(
        srcEle: WebElement,
        tagName: String,
        index: Int = 0,
        reverse: Boolean = false,
    ) {
        FindNeighborForEle(srcEle, {
            it.findElements(By.tagName(tagName))
        }, index, reverse)
    }

    fun FindNeighborForEle(
        srcEle: WebElement,
        byLam: (WebElement) -> (List<WebElement>),
        index: Int = 0,
        reverse: Boolean = false,
    ): WebElement? {
        val aimList = byLam(srcEle)
        if (aimList.isNotEmpty()) {
            if (reverse) {
                aimList.reversed()
            }
            if (aimList.size > index) {
                return aimList[index]
            } else {
                return null
            }
        }
        val p = srcEle.findElement(By.xpath("./.."))
        return FindNeighborForEle(p, byLam, index, reverse)
    }
}