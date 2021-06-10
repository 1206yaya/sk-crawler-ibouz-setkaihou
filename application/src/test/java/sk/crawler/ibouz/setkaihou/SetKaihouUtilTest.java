package sk.crawler.ibouz.setkaihou;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import sk.crawler.ibouz.setkaihou.config.SetPattern;

public class SetKaihouUtilTest {
	@Test
	public void divideTest() {
		int _1_idSize = 5;
		int _1_idBlockSize = 2;
		List<String> _1_Ids = List.of("1", "2", "3", "4", "5");
		SetPattern _1_Pattern = new SetPattern(_1_idSize, _1_idBlockSize, _1_Ids);
		
		int _2_idSize = 7;
		int _2_idBlockSize = 3;
		List<String> _2_Ids = List.of("11", "12", "13", "14", "15", "16", "17");
		SetPattern _2_Pattern = new SetPattern(_2_idSize, _2_idBlockSize, _2_Ids);
		
		
		
		Set<SetPattern> patterns = new HashSet<>();
		patterns.add(_1_Pattern);
		patterns.add(_2_Pattern);
		
		int setSize = 10;
		
		List<List<String>> concatList = SetKaihouUtil.divide(setSize, patterns);
		
		System.out.println(concatList);
	}
}
