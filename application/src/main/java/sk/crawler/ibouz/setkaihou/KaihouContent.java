package sk.crawler.ibouz.setkaihou;

import lombok.Getter;
import lombok.Setter;




@Getter
@Setter
public class KaihouContent {
	public KaihouContent(String title, String message) {
		this.title = title;
		this.message = message;
	}
	private String title;
	private String message;
}
