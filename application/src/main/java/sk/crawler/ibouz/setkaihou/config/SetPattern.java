package sk.crawler.ibouz.setkaihou.config;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetPattern {
	private int idSize;
	private int blockSize;
	private List<String> ids;
	private List<List<String>> idsBlockList;
	

	public SetPattern(int idSize, int blockSize, List<String> ids) {
		this.idSize = idSize;
		this.blockSize = blockSize;
		this.ids = ids;
		this.idsBlockList = new ArrayList<List<String>>();
		
		for (List<String> block : Iterables.partition(ids, blockSize)) {
			this.idsBlockList.add(block);
		}
		
	}
	
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private int nextNum = 0;
	/**
	 * idsBlockListから順々にidsBlockを渡す
	 * @return
	 */
	public List<String> getNextIdsBlock() {
		// 最後まで回したら初期化
		if (nextNum == idsBlockList.size()) {
			nextNum = 0;
		}
		return idsBlockList.get(nextNum++);
	}

	
	
	
}
