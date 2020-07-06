package sk.crawler.ibouz.setkaihou;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;

import sk.crawler.ibouz.library.config.PathConfig;

public class CrawlerTest {
	@BeforeAll
	public static void setUp() throws IOException {
		Configuration.holdBrowserOpen = false;
		Configuration.browser = WebDriverRunner.CHROME;
		Configuration.headless = true;
		if (System.getProperty("os.name").contains("Windows")) {
			System.setProperty("webdriver.chrome.driver", PathConfig.CHROMEDRIVER_FOR_WIN_PATH);
		} else {
			System.setProperty("webdriver.chrome.driver", PathConfig.CHROMEDRIVER_FOR_LINUX_PATH);
			System.out.println("OS IS LINUX");
		}
	}

	@Test
	public void crawler() throws Exception {
		GooglePage googlepage = open("https://www.google.co.jp", GooglePage.class);
		String actualTitile = googlepage.context();
		System.out.println("TITLE: " + actualTitile);
	}
}

class GooglePage {

	public String context() {
		System.out.println(">>>> GooglePage#context " + $(By.tagName("body")).getText());
		return $(By.tagName("body")).getText();
	}
}
