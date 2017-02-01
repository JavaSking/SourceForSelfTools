package javasking.directory;

import java.io.File;
import java.io.FileFilter;

/**
 * 文件操作工具类。
 * 
 * @author JavaSking 2017年1月15日
 */
public class FileTools {

	public static void main(String[] args) {

		String filepath = "D:\\DownLoad\\Alexa Grace";
		String indent = "  ";
		FileFilter filter = new Directory();
		FileTools.printDirectory(filepath, 0, indent, filter);
	}

	/**
	 * 打印文件目录树。
	 * 
	 * @param path
	 *          文件路径。
	 * 
	 * @param level
	 *          文件层级。
	 * @param indent
	 *          缩进符。
	 * @param filter
	 *          文件过滤器。
	 */
	public static void printDirectory(String path, int level, String indent, FileFilter filter) {

		File file = new File(path);
		if (file.isFile()) {
			if (filter == null || (filter != null && filter.accept(file))) {
				for (int i = 1; i <= level; i++) {
					System.out.print(indent);
				}
				System.out.println(file.getName());
			}
		} else if (file.isDirectory()) {
			for (int i = 1; i <= level; i++) {
				System.out.print(indent);
			}
			System.out.println(file.getName());
			File[] subFiles = file.listFiles();
			for (File subFile : subFiles) {
				printDirectory(subFile.getAbsolutePath(), level + 1, indent, filter);
			}
		}
	}
}

/**
 * MP3文件。
 * 
 * @author JavaSking 2017年1月15日
 */
class MP3File implements FileFilter {

	/**
	 * MP3文件后缀。
	 */
	private static final String MP3 = ".mp3";

	public boolean accept(File pathname) {

		if (pathname.exists() && pathname.isFile() && pathname.getName().endsWith(MP3)) {
			return true;
		}
		return false;
	}
}

/**
 * 目录。
 * 
 * @author JavaSking 2017年2月1日
 */
class Directory implements FileFilter {

	public boolean accept(File pathname) {

		if (pathname.exists() && pathname.isDirectory()) {
			return true;
		}
		return false;
	}
}
