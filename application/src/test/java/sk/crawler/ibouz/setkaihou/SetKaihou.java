package sk.crawler.ibouz.setkaihou;

import static com.codeborne.selenide.Selenide.open;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import sk.crawler.ibouz.library.domain.Ibouz;
import sk.crawler.ibouz.library.domain.IbouzBuilder;
import sk.crawler.ibouz.library.pages.LoginPage;
import sk.crawler.ibouz.library.util.CrawlerEnv;
import sk.crawler.ibouz.library.util.WebDriverUtil;
import sk.crawler.ibouz.setkaihou.config.CorePathConfig;
import sk.crawler.ibouz.setkaihou.config.SetPattern;

/**
 * 
 * 【2021/6/8 前】 int etcIdSize = 180000; int docomoIdSize = 18000; int
 * etcIdBlockSize = 6000; int docomoIdBlockSize = 300; int kaihousetSize = 540;
 */
@SpringBootTest
public class SetKaihou {
	static Ibouz ibouz;


	@Value("#{systemProperties['env']}")
	CrawlerEnv env;

	@Autowired
	WebDriverUtil webDriverUtil;

	private File titleFile = CorePathConfig.TITLE_FILE;
	public void setUp() throws IOException {
		File settingFile = CorePathConfig.SETTING_FILE;
		if (env == null || env.equals(CrawlerEnv.IS_DEV)) {
			env = CrawlerEnv.IS_DEV;
			settingFile = CorePathConfig.DEV_SETTING_FILE;
			titleFile = CorePathConfig.DEV_TITLE_FILE;
		}
		webDriverUtil.setUp(env);
		

		List<String> settings = FileUtils.readLines(settingFile, StandardCharsets.UTF_8);
		ibouz = IbouzBuilder.createIbouz(settings.get(0), settings.get(1), settings.get(2), settings.get(3));
	}
	
	@Test
	public void crawler() throws Exception {
		setUp();
		List<String> titles = FileUtils.readLines(titleFile, StandardCharsets.UTF_8);
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		LocalDate tomorrow = today.plusDays(1);;

		LocalDateTime collectStartDay = LocalDateTime.of(yesterday.getYear(), yesterday.getMonth(),
				yesterday.getDayOfMonth(), 23, 59);
		LocalDateTime kaihousetStartDay = LocalDateTime.of(tomorrow.getYear(), tomorrow.getMonth(),
				tomorrow.getDayOfMonth(), 6, 01);
		
		login();
		
		/** 
		 * 条件１
		 */
		String _1_tokusyuStatus = "デフォルト";
		int kaihousetSize = 540;
		int etcIdSize = 150000;
		int etcIdBlockSize = 4500;
		int docomoIdSize = 18000;
		int docomoIdBlockSize = 300;
		// テスト用
//		int kaihousetSize = 5;
//		int etcIdSize = 5;
//		int etcIdBlockSize = 2;
//		int docomoIdSize = 7;
//		int docomoIdBlockSize = 3;
		
		
		SetKaihouUtil skUtil = new SetKaihouUtil(ibouz, collectStartDay);

		// ID抽出
		List<String> etcIds = skUtil.getIds(etcIdSize, List.of("AU", "SoftBank", "WILLCOM", "PC"), 6, _1_tokusyuStatus);
		List<String> docomoIds = skUtil.getIds(docomoIdSize, List.of("DoCoMo"), 6, _1_tokusyuStatus);
		// Blocksの作成
		SetPattern etcPattern = new SetPattern(etcIdSize, etcIdBlockSize, etcIds);
		SetPattern docomoPattern = new SetPattern(docomoIdSize, docomoIdBlockSize, docomoIds);
		Set<SetPattern> _1_patterns = new HashSet<SetPattern>();
		Collections.addAll(_1_patterns, etcPattern, docomoPattern);

		List<List<String>> _1_allIds = skUtil.divide(kaihousetSize, _1_patterns);
		skUtil.setKaihou(titles, kaihousetSize, kaihousetStartDay, _1_allIds);
		
		/**
		 * 条件2
		 */
		String _2_tokusyuStatus = "一軍";
		int _2_idSize = 30000;
		int _2_idBlockSize = 1500;
		// テスト用
//		int _2_idSize = 5;
//		int _2_idBlockSize = 2;
		
		List<String> _2_ids = skUtil.getIds(_2_idSize, List.of("AU", "SoftBank", "WILLCOM", "PC", "DoCoMo"), 6, _2_tokusyuStatus);
		SetPattern _2_setPattern = new SetPattern(_2_idSize, _2_idBlockSize, _2_ids);
 		List<List<String>> _2_allIds = skUtil.divide(kaihousetSize, Collections.singleton(_2_setPattern));
		skUtil.setKaihou(titles, kaihousetSize, kaihousetStartDay, _2_allIds);

	}


	private void login() {
		LoginPage loginPage = open(ibouz.getLogintURL(), LoginPage.class);
		loginPage.login(ibouz.getLoginid(), ibouz.getLoginpw());
	}

}
