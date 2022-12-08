package com.rulebased848.gamsibot.core;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import static org.openqa.selenium.OutputType.BYTES;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ScreenshotTaker {
    public ScreenshotTaker(@Value("${path.chrome-driver}") String pathToChromeDriver) {
        System.setProperty("webdriver.chrome.driver", pathToChromeDriver);
    }

    public DataHandler getScreenshot(String url) {
        RemoteWebDriver driver = new ChromeDriver();
        driver.get(url);
        byte[] imageBytes = driver.getScreenshotAs(BYTES);
        driver.quit();
        DataSource imageDataSource = new ByteArrayDataSource(imageBytes, "image/png");
        return new DataHandler(imageDataSource);
    }
}