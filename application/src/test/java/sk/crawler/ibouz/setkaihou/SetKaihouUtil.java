package sk.crawler.ibouz.setkaihou;

import static com.codeborne.selenide.Selenide.open;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.common.collect.Iterables;

import sk.crawler.ibouz.library.domain.Ibouz;
import sk.crawler.ibouz.library.pages.KaihouEditPage;
import sk.crawler.ibouz.library.pages.UserSearchPage;
import sk.crawler.ibouz.library.pages.UserSearchResultPage;
import sk.crawler.ibouz.setkaihou.config.SetPattern;

public class SetKaihouUtil {
	Ibouz ibouz;
	LocalDateTime collectStartDay;

	public SetKaihouUtil(Ibouz ibouz, LocalDateTime collectStartDay) {
		this.ibouz = ibouz;
		this.collectStartDay = collectStartDay;
	}

	/**
	 * IDの取得 検索結果が制限Overにならないよう6時間区切りで検索をする
	 */
	public List<String> getIds(int idSize, List<String> carriers, int splitHour, String tokusyuStatus) {
		System.out.println(idSize + "個のIDが取得できるまで検索を昨日の23:59～6時間ごとに検索し続けます");
		Set<String> ids = new HashSet<>();
		LocalDateTime targetDay = collectStartDay;
		LocalDateTime limit = targetDay.minusDays(7);
		while (ids.size() < idSize) {
			// limitよりもtargetDayが古い日付になったら検索を停止する
			if (targetDay.isBefore(limit)) {
				ids = new HashSet<>();
				break;
			}
			UserSearchPage userSearchPage = open(ibouz.getUserSearchURL(), UserSearchPage.class);
			// メールキャリア
			userSearchPage.setMailCarrier(carriers);
			// 累計購入回数
			userSearchPage.setBuyCount(0);
			// 初回ログイン 未ログインON
			userSearchPage.clickNewaccLoginNotLogin();
			// 登録日時: 前日の17:59~23:59からスタートする
			LocalDateTime until = targetDay;
			LocalDateTime since = targetDay.minusHours(splitHour);
			userSearchPage.setRegistrationTime(since, until);
			targetDay = targetDay.minusHours(splitHour);
			// 特殊ステータス
			userSearchPage.setTokusyu(tokusyuStatus);
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
			int diff = idSize - ids.size() < 0 ? 0 : idSize - ids.size();

			System.out.println(
					carriers.toString() + " " + "since:" + since + " until:" + until + " 検索結果数:" + resultIds.size() + " 残りのID数:" + diff);
//			StringBuilder sb = new StringBuilder();
//			for (int i = 0; i < resultIds.size(); i++) {
//				sb.append(resultIds.get(i) + " ");
//			}
//			System.out.println(sb.toString());
			
		}
		List<String> kaihouIds = new ArrayList<>();
		if (ids.size() == 0) {
			return kaihouIds;
		}
		kaihouIds = Iterables.partition(ids, idSize).iterator().next();
		
		StringBuilder sb = new StringBuilder();
		kaihouIds.forEach(id -> sb.append(id + " "));
//		System.out.println("会報送信のためのIDs： " + sb.toString());
		
		return kaihouIds;
	}

	private void setKaihouContent(KaihouEditPage kaihouEditPage, String title,String message, LocalDateTime targetKaihouSetDay) {
		kaihouEditPage.setTitle(title);
		kaihouEditPage.setMessage(message);
		kaihouEditPage.setReservationDateAndTime(targetKaihouSetDay.getYear(), targetKaihouSetDay.getMonthValue(),
				targetKaihouSetDay.getDayOfMonth(), targetKaihouSetDay.getHour(), targetKaihouSetDay.getMinute());
	}

	public void setKaihou(List<KaihouContent> contents, int kaihousetSize, LocalDateTime kaihousetStartDay, List<List<String>> allIds) {
		/**
		 * 3. 会報送信セット 翌日6:01から23:59まで、合計kaihousetSize = 540 回分のセットを行う
		 */
		int titleCount = 0;
		LocalDateTime targetKaihouSetDay = kaihousetStartDay;
		for (int i = 0; i < kaihousetSize; i++) {
			long start = System.currentTimeMillis();
			List<String> ids = allIds.get(i);
//			System.out.println("抽出ID：" + ids);
			UserSearchPage userSearchPage = open(ibouz.getUserSearchURL(), UserSearchPage.class);
			userSearchPage.setIds(ids);

			// 初回ログイン 未ログインON
			userSearchPage.clickNewaccLoginNotLogin();
			// メインメールエラー回数 0 ~ 1 
			userSearchPage.setMailErrorNum(0, 1);
			UserSearchResultPage userSearchResultPage = userSearchPage.search();
			KaihouEditPage kaihouEditPage = userSearchResultPage.clickKaihouYoyaku();

			if (titleCount == contents.size()) {
				titleCount = 0;
			}
			KaihouContent content = contents.get(titleCount++);
			String title = content.getTitle();
			String message = content.getMessage();
			
			setKaihouContent(kaihouEditPage, title, message, targetKaihouSetDay);
			long end = System.currentTimeMillis();
			String thisTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
			System.out.println(thisTime + " " + formatedTime(end - start) + ":" + (i+1) + "/" + kaihousetSize
					+ " idSize:" + ids.size() + " title:" + title  + " message:" + message +  " 予約日時: " + targetKaihouSetDay);
			System.out.println();
			targetKaihouSetDay = targetKaihouSetDay.plusMinutes(2);
			kaihouEditPage.submit();
			kaihouEditPage.changeNewWindow();
		}
	}
	
	public String formatedTime(long millis) {
		return String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
	}

	public static List<List<String>> divide(int kaihousetSize, Set<SetPattern> setPatterns) {
		List<List<String>> allIds = new ArrayList<List<String>>(); 

		for (int i = 1; i <= kaihousetSize; i++) {
			List<String> concatIds = new ArrayList<>();
			
			setPatterns.forEach(pattern -> {
				concatIds.addAll(pattern.getNextIdsBlock());
			});
			
			allIds.add(concatIds);
		}
		return allIds;
	}

	
	public static List<KaihouContent> getContents(String filePath) throws IOException {
        
		List<KaihouContent> contents = new ArrayList<>();
	    FileInputStream fis = new FileInputStream(filePath);
	    BufferedReader br = null;
        CSVParser parse = null;
        
        InputStreamReader isr = new InputStreamReader(fis, "shift-jis");
        br = new BufferedReader(isr);
        // CSVファイルをパース
        parse = CSVFormat.EXCEL.parse(br);
        // レコードのリストに変換
        List<CSVRecord> recordList = parse.getRecords();
        
        // 各レコードを標準出力に出力
        for (CSVRecord record : recordList) {
        	KaihouContent content = new KaihouContent(record.get(0), record.get(1));
        	contents.add(content);
        }
		return contents;
	}


	
	
	
	
	
	
	
	
	
	
}
