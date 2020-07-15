package sk.crawler.ibouz.setkaihou;

import static com.codeborne.selenide.Selenide.open;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.common.collect.Iterables;

import sk.crawler.ibouz.library.domain.Ibouz;
import sk.crawler.ibouz.library.domain.IbouzBuilder;
import sk.crawler.ibouz.library.pages.KaihouEditPage;
import sk.crawler.ibouz.library.pages.LoginPage;
import sk.crawler.ibouz.library.pages.UserSearchPage;
import sk.crawler.ibouz.library.pages.UserSearchResultPage;
import sk.crawler.ibouz.library.util.CrawlerEnv;
import sk.crawler.ibouz.library.util.WebDriverUtil;
import sk.crawler.ibouz.setkaihou.config.CorePathConfig;

@SpringBootTest
public class SetKaihou {
	static Ibouz ibouz;
	LocalDate today = LocalDate.now();
	LocalDate yesterday = today.minusDays(1);
	LocalDate tomorrow = today.plusDays(1);;

	LocalDateTime collectStartDay = LocalDateTime.of(yesterday.getYear(), yesterday.getMonth(),
			yesterday.getDayOfMonth(), 23, 59);
	LocalDateTime kaihousetStartDay = LocalDateTime.of(tomorrow.getYear(), tomorrow.getMonth(),
			tomorrow.getDayOfMonth(), 6, 01);

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

	int etcIdSize = 180000;
	int docomoIdSize = 9000;
	int etcIdBlockSize = 3000;
	int docomoIdBlockSize = 150;
	int kaihousetSize = 540;
	// テスト用
//	int etcIdSize = 3000;
//	int docomoIdSize = 150;
//	int etcIdBlockSize = 1000;
//	int docomoIdBlockSize = 50;

	@Test
	public void crawler() throws Exception {
		setUp();
		List<String> titles = FileUtils.readLines(titleFile, StandardCharsets.UTF_8);

		login();
		/**
		 * 1. IDの取得 検索結果が制限Overにならないよう6時間区切りで検索をする
		 */
		List<String> etcIds = getIds(etcIdSize, List.of("AU", "SoftBank", "WILLCOM", "PC"), 6);
		List<String> docomoIds = getIds(docomoIdSize, List.of("DoCoMo"), 6);
		/**
		 * 2. 一つの会報送信にセットするID軍を作成する
		 */
		List<List<String>> etcIdblockList = new ArrayList<List<String>>(); // 60 Blocks
		List<List<String>> docomoIdblockList = new ArrayList<List<String>>(); // 60 Blocks
		for (List<String> block : Iterables.partition(etcIds, etcIdBlockSize)) {
			etcIdblockList.add(block);
		}
		for (List<String> block : Iterables.partition(docomoIds, docomoIdBlockSize)) {
			docomoIdblockList.add(block);
		}

		/**
		 * 3. 会報送信セット 翌日6:01から23:59まで、合計kaihousetSize = 540 回分のセットを行う
		 */
		int blockCount = 0;
		int titleCount = 0;
		LocalDateTime targetKaihouSetDay = kaihousetStartDay;
		for (int i = 1; i <= kaihousetSize; i++) {
			long start = System.currentTimeMillis();
			if (blockCount == etcIdblockList.size()) {
				blockCount = 0;
			}
			if (titleCount == titles.size()) {
				titleCount = 0;
			}
			List<String> etcIdBlock = etcIdblockList.get(blockCount);
			List<String> docomoIdBlock = docomoIdblockList.get(blockCount);
			blockCount++;
			List<String> allIds = Stream.concat(etcIdBlock.stream(), docomoIdBlock.stream())
					.collect(Collectors.toList());

			UserSearchPage userSearchPage = open(ibouz.getUserSearchURL(), UserSearchPage.class);
			userSearchPage.setIds(allIds);
			// 初回ログイン 未ログインON
			userSearchPage.clickNewaccLoginNotLogin();
			UserSearchResultPage userSearchResultPage = userSearchPage.search();
			KaihouEditPage kaihouEditPage = userSearchResultPage.clickKaihouYoyaku();
			String title = titles.get(titleCount++);

			setKaihou(kaihouEditPage, title, targetKaihouSetDay);
			long end = System.currentTimeMillis();
			System.out.println(formatedTime(end - start) + ":" + i + "/" + kaihousetSize + " idSize:" + allIds.size()
					+ " title:" + title + " 予約日時: " + targetKaihouSetDay);
			targetKaihouSetDay = targetKaihouSetDay.plusMinutes(2);
			kaihouEditPage.submit();
			kaihouEditPage.changeNewWindow();
		}

	}

	private void setKaihou(KaihouEditPage kaihouEditPage, String title, LocalDateTime targetKaihouSetDay) {
		kaihouEditPage.setTitle(title);
		kaihouEditPage.setMessage("%site_url%blanc.php?id=%id%&pass=%pass%");
		kaihouEditPage.setReservationDateAndTime(targetKaihouSetDay.getYear(), targetKaihouSetDay.getMonthValue(),
				targetKaihouSetDay.getDayOfMonth(), targetKaihouSetDay.getHour(), targetKaihouSetDay.getMinute());
	}

	private List<String> getIds(int idSize, List<String> carriers, int splitHour) {
		Set<String> ids = new HashSet<>();
		LocalDateTime targetDay = collectStartDay;
		while (ids.size() <= idSize) {
			UserSearchPage userSearchPage = open(ibouz.getUserSearchURL(), UserSearchPage.class);
			// メールキャリア
			for (String carrier : carriers) {
				userSearchPage.setMailCarrier(carrier);
			}
			// 累計購入回数
			userSearchPage.setBuyCount(0);
			// 初回ログイン 未ログインON
			userSearchPage.clickNewaccLoginNotLogin();
			// 登録日時
			LocalDateTime until = targetDay;
			LocalDateTime since = targetDay.minusHours(splitHour);
			userSearchPage.setRegistrationTime(since, until);
			targetDay = targetDay.minusHours(splitHour);

			UserSearchResultPage userSearchResultPage = userSearchPage.search();
			int resultCount = userSearchResultPage.getResultCount();
			List resultIds = null;
			if (resultCount == 0) {
				resultIds = new ArrayList<>();
			} else {
				resultIds = userSearchResultPage.getTotalIdList();
			}

			ids.addAll(resultIds);
			userSearchResultPage.changeNewWindow();
			System.out.println(
					carriers.toString() + " " + "since:" + since + " until:" + until + " result:" + resultIds.size());
		}
		;
		return Iterables.partition(ids, idSize).iterator().next();
	}

	private void login() {
		LoginPage loginPage = open(ibouz.getLogintURL(), LoginPage.class);
		loginPage.login(ibouz.getLoginid(), ibouz.getLoginpw());
	}

	private String formatedTime(long millis) {
		return String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
	}

}
