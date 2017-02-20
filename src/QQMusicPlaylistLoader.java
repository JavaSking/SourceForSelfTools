import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QQMusicPlaylistLoader {

	public static final String PLAYLIST = "class=\"playlist__item\"";

	public static final String TITLE = "title=\"";

	public static final String DATE = "class=\"playlist__other\">";

	public static final String COVER = "<img src=\"";

	public static final String QUOTA = "\"";

	public static final String DIV = "</div>";

	public static final String QQMUSIC = "https://y.gtimg.cn/music/photo_new/";

	public QQMusicPlaylistLoader() {

	}

	public void createPlayListDirectory() {

		String directoryPath = this.getClass().getResource("/").getPath().substring(1);
		File directory = new File(directoryPath);
		String saveRootpath = directoryPath + "Albums";
		File saveRootDir = new File(saveRootpath);
		if (!saveRootDir.exists()) {
			saveRootDir.mkdirs();
		}
		File[] subFiles = directory.listFiles();
		for (File subFile : subFiles) {
			String filename = subFile.getName();
			if (subFile.isFile() && filename.toLowerCase().endsWith(".html")) {
				String singer = filename.substring(0, filename.lastIndexOf("."));
				String singerDir = saveRootpath + File.separator + singer;
				new File(singerDir).mkdirs();
				Map<String, String> playList = getPlayList(readFromSingleFile(subFile));
				Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
				for (String dateAndTitle : playList.keySet()) {
					Matcher matcher = pattern.matcher(dateAndTitle);
					String legalDir = matcher.replaceAll(" ");
					String playListDir = singerDir + File.separator + legalDir;
					new File(playListDir).mkdirs();
					String cover = playListDir + File.separator + legalDir + ".jpg";
					try {
						URL url = new URL(playList.get(dateAndTitle));
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setConnectTimeout(10 * 1000);
						connection.setReadTimeout(10 * 1000);
						connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
						InputStream inputStream = connection.getInputStream();
						byte[] buffer = new byte[1024];
						int len = 0;
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						while ((len = inputStream.read(buffer)) != -1) {
							bos.write(buffer, 0, len);
						}
						bos.close();
						FileOutputStream fos = new FileOutputStream(cover);
						fos.write(bos.toByteArray());
						if (inputStream != null) {
							inputStream.close();
						}
						if (fos != null) {
							fos.close();
						}
						connection.disconnect();
					} catch (IOException ex) {
						System.err.println("\nLoading cover:" + playList.get(dateAndTitle) + "，save path:" + cover + " failure\n");
					}
				}
			}
		}
	}

	private static String readFromSingleFile(File file) {

		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line).append("\n");
			}
			if (reader != null) {
				reader.close();
			}
		} catch (Exception ex) {
			System.err.println("Loading source file[" + file.getAbsolutePath() + "]failure!");
		}
		String fileContent = buffer.toString();
		try {
			fileContent = new String(fileContent.getBytes(), "UTF-8");
			System.out.println(fileContent);
		} catch (Exception ex) {
			System.err.println("Decoding source file[" + file.getAbsolutePath() + "]failure!");
		}
		return fileContent;
	}

	private static Map<String, String> getPlayList(String content) {

		Map<String, String> playList = new HashMap<String, String>();
		String[] parts = content.toString().split(PLAYLIST);
		for (int index = 1; index < parts.length; index++) {
			String part = parts[index];
			int titleStartIndex = part.indexOf(TITLE);
			int titleEndIndex = part.indexOf(QUOTA, titleStartIndex + TITLE.length() + 1);
			String title = part.substring(titleStartIndex + TITLE.length(), titleEndIndex);
			int dateStartIndex = part.indexOf(DATE);
			int dateEndIndex = part.indexOf(DIV, dateStartIndex + DATE.length() + 1);
			String date = part.substring(dateStartIndex + DATE.length(), dateEndIndex).trim();
			int coverStartIndex = part.indexOf(COVER);
			int coverEndIndex = part.indexOf(QUOTA, coverStartIndex + COVER.length() + 1);
			String relativeAddress = part.substring(coverStartIndex + COVER.length(), coverEndIndex);
			String absoluteAddress = QQMUSIC + relativeAddress.substring(relativeAddress.lastIndexOf("/") + 1);
			playList.put(date + " " + title, absoluteAddress);
		}
		return playList;
	}

	public static void main(String[] args) {

		QQMusicPlaylistLoader tool = new QQMusicPlaylistLoader();
		tool.createPlayListDirectory();
	}
}
