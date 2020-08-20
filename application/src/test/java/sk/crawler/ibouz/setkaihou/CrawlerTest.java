package sk.crawler.ibouz.setkaihou;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;

import sk.crawler.ibouz.library.config.PathConfig;
import sk.crawler.ibouz.library.util.CrawlerEnv;
import sk.crawler.ibouz.library.util.WebDriverUtil;
@SpringBootTest
public class CrawlerTest {
	@Autowired
	WebDriverUtil webDriverUtil;
	
	@Value("#{systemProperties['env']}")
	CrawlerEnv env;

	@Test
	public void crawler() throws Exception {
		webDriverUtil.setUp(env);
		
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
