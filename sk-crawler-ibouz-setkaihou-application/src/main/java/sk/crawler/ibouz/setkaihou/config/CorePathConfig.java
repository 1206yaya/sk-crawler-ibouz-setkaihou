package sk.crawler.ibouz.setkaihou.config;

import java.io.File;
import java.nio.file.Paths;

import sk.crawler.ibouz.library.config.PathConfig;

public class CorePathConfig {
	
	public static  final File TITLE_FILE = Paths.get(PathConfig.PROJ_DIR, "TITLES.txt").toFile();
	public static  final File SETTING_FILE = Paths.get(PathConfig.PROJ_DIR, "SETTINGS.txt").toFile();

}
