package com.rulebased848.gamsibot.core;

import java.net.MalformedURLException;
import java.net.URL;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import org.openqa.selenium.Capabilities;
import static org.openqa.selenium.OutputType.BYTES;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ScreenshotTaker {
    private final URL webdriverUrl;

    public ScreenshotTaker(@Value("${webdriver.url}") String webdriverUrl) throws MalformedURLException {
        this.webdriverUrl = new URL(webdriverUrl);
    }

    public DataHandler getScreenshot(String url) {
        Capabilities capabilities = new ChromeOptions();
        RemoteWebDriver driver = new RemoteWebDriver(webdriverUrl, capabilities);
        driver.get(url);
        byte[] imageBytes = driver.getScreenshotAs(BYTES);
        driver.quit();
        DataSource imageDataSource = new ByteArrayDataSource(imageBytes, "image/png");
        return new DataHandler(imageDataSource);
    }
}