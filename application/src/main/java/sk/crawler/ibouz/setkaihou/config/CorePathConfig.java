package sk.crawler.ibouz.setkaihou.config;

import java.io.File;
import java.nio.file.Paths;

public class CorePathConfig {
	public final static String PROJ_DIR = Paths.get(System.getProperty("user.dir")).toString();
	public static  final File TITLE_FILE = Paths.get(PROJ_DIR, "TITLES.txt").toFile();
	public static  final File SETTING_FILE = Paths.get(PROJ_DIR, "SETTINGS.txt").toFile();
	
	public static  final File DEV_TITLE_FILE = Paths.get(PROJ_DIR, "DEV_TITLES.txt").toFile();
	public static  final File DEV_SETTING_FILE = Paths.get(PROJ_DIR, "DEV_SETTINGS.txt").toFile();
}
