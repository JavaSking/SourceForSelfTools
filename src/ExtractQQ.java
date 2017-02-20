

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;

public class ExtractQQ {

	public static final FileFilter filter = new HTMLFile();

	public void extractQQ() {

		String directory = this.getClass().getResource("/").getPath().substring(1);
		String saveFile = directory + "客户邮箱列表.txt";
		File[] subFiles = new File(directory).listFiles();
		StringBuffer result = new StringBuffer();
		for (File subFile : subFiles) {
			if (filter.accept(subFile)) {
				extractQQFromFile(subFile, result);
			}
		}
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(saveFile));
			writer.write(result.toString());
			if (writer != null) {
				writer.close();
			}
		} catch (Exception ex) {
			System.err.println("写文件[" + saveFile + "]失败！");
		}
	}

	public void extractQQFromFile(File file, StringBuffer result) {

		BufferedReader reader = null;
		StringBuffer buffer = new StringBuffer();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line).append("\n");
			}
		} catch (Exception ex) {
			System.err.println("解析文件[" + file.getAbsolutePath() + "]失败！");
		}
		String[] parts = buffer.toString().split("<span class=\"white\">", -1);
		int count = 0;
		for (int i = 1; i < parts.length - 1; i++) {
			String part = parts[i];
			int start = parts[i].indexOf("<td>");
			int end = parts[i].indexOf("</td>", start + 5);
			String qq = part.substring(start + 4, end).trim();
			result.append(qq).append("@qq.com").append(";");
			count++;
			if (count % 5 == 0) {
				result.append("\n");
			}
		}
		result.append("\n");
	}

	public static void main(String[] args) {

		ExtractQQ test = new ExtractQQ();
		test.extractQQ();
	}
}

class HTMLFile implements FileFilter {

	public boolean accept(File pathname) {

		if (pathname.exists() && pathname.isFile() && pathname.getName().toLowerCase().endsWith(".html")) {
			return true;
		}
		return false;
	}
}
