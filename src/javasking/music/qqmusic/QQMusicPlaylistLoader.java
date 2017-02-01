package javasking.music.qqmusic;
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

/**
 * QQ音乐专辑采集工具<br>
 * 
 * 1、分歌手创建目录<br>
 * 2、在歌手目录下分专辑创建目录
 * 
 * @author JavaSking 2017-01-15
 */
public class QQMusicPlaylistLoader {
	
	/**
	 * 专辑标签。
	 */
	public static final String PLAYLIST = "class=\"playlist__item\"";

	/**
	 * 专辑名标签。
	 */
	public static final String TITLE = "title=\"";

	/**
	 * 专辑发布日期标签。
	 */
	public static final String DATE = "class=\"playlist__other\">";

	/**
	 * 专辑封面标签。
	 */
	public static final String COVER = "<img src=\"";

	/**
	 * 双引号。
	 */
	public static final String QUOTA = "\"";

	/**
	 * div标签。
	 */
	public static final String DIV = "</div>";

	/**
	 * QQ音乐静态资源域。
	 */
	public static final String QQMUSIC = "https://y.gtimg.cn/music/photo_new/";

	/**
	 * 专辑保存根目录:<br>
	 * 根目录 <br>
	 * --歌手1 <br>
	 * ----歌手1专辑1 <br>
	 * ----歌手2专辑2 <br>
	 *     ... <br>
	 * --歌手2 <br>
	 * ----歌手2专辑1 <br>
	 * ----歌手2专辑2<br>
	 *     ...<br>
	 * ...<br>
	 */
	private String saveRootpath;

	/**
	 * 专辑介绍文件目录。
	 */
	private String introduceDir;

	/**
	 * 构造函数。
	 * 
	 * @param saveRootpath
	 *          专辑保存根目录。
	 * @param introduceDir
	 *          专辑介绍文件目录。
	 */
	public QQMusicPlaylistLoader(String saveRootpath, String introduceDir) {

		this.saveRootpath = saveRootpath;
		this.introduceDir = introduceDir;
	}

	/**
	 * 创建歌手专辑目录。
	 */
	public void createPlayListDirectory() {

		File directory = new File(introduceDir);
		/* 1、创建专辑保存根目录。 */
		File saveRootDir = new File(saveRootpath);
		if (!saveRootDir.exists()) {
			saveRootDir.mkdirs();
		}
		File[] subFiles = directory.listFiles();
		for (File subFile : subFiles) {
			String filename = subFile.getName();
			if (subFile.isFile() && filename.endsWith(".html")) {//只处理HTML文件
				/* 2、提取歌手名，创建歌手目录 */
				String singer = filename.substring(0, filename.lastIndexOf("."));
				String singerDir = saveRootpath + File.separator + singer;
				new File(singerDir).mkdirs();
				/* 3、获取该歌手专辑列表 */
				Map<String, String> playList = getPlayList(readFromSingleFile(subFile));
				Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
				for (String dateAndTitle : playList.keySet()) {
					/* 4、创建专辑目录（处理目录中的非法字符） */
					Matcher matcher = pattern.matcher(dateAndTitle);
					String legalDir = matcher.replaceAll(" ");
					String playListDir = singerDir + File.separator + legalDir;
					new File(playListDir).mkdirs();
					/* 5、下载专辑封面 */
					String cover = playListDir + File.separator + legalDir + ".jpg";
					try {
						URL url = new URL(playList.get(dateAndTitle));
						System.out.println("\n开始下载专辑封面:" + legalDir + "，下载地址：" + playList.get(dateAndTitle) + "\n");
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setConnectTimeout(10 * 1000);
						connection.setReadTimeout(10 * 1000);
						/* 设置浏览器代理，解决部分连接拒绝问题 */
						connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
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
						System.err.println("\n下载专辑封面：" + playList.get(dateAndTitle) + "，本地保存地址：" + cover + " 失败\n");
					}
				}
			}
		}
	}

	/**
	 * 读取目标文件内容。
	 * 
	 * @param file
	 *          待读取的目标文件。
	 * @return 文件内容。
	 */
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
			System.err.println("读取目标文件[" + file.getAbsolutePath() + "]失败!");
		}
		String fileContent = buffer.toString();
		try {
			fileContent = new String(fileContent.getBytes(), "UTF-8");
			System.out.println(fileContent);
		} catch (Exception ex) {
			System.err.println("解码目标文件[" + file.getAbsolutePath() + "]失败!");
		}
		return fileContent;
	}

	/**
	 * 获取专辑列表信息（key：发布日期 专辑名 value：专辑封面地址）。
	 * 
	 * @param content 专辑介绍内容。
	 * 
	 * @return 专辑列表信息。
	 */
	private static Map<String, String> getPlayList(String content) {

		Map<String, String> playList = new HashMap<String, String>();
		String[] parts = content.toString().split(PLAYLIST);
		for (int index = 1; index < parts.length; index++) {
			String part = parts[index];
			/*1、采集专辑名 */
			int titleStartIndex = part.indexOf(TITLE);
			int titleEndIndex = part.indexOf(QUOTA, titleStartIndex + TITLE.length() + 1);
			String title = part.substring(titleStartIndex + TITLE.length(), titleEndIndex);
			/*2、采集专辑发布日期 */
			int dateStartIndex = part.indexOf(DATE);
			int dateEndIndex = part.indexOf(DIV, dateStartIndex + DATE.length() + 1);
			String date = part.substring(dateStartIndex + DATE.length(), dateEndIndex).trim();
			/*3、采集专辑封面地址 */
			int coverStartIndex = part.indexOf(COVER);
			int coverEndIndex = part.indexOf(QUOTA, coverStartIndex + COVER.length() + 1);
			String relativeAddress = part.substring(coverStartIndex + COVER.length(), coverEndIndex);//相对地址ַ
			String absoluteAddress = QQMUSIC + relativeAddress.substring(relativeAddress.lastIndexOf("/") + 1);//绝对地址ַ
			System.out.println(date + " " + title + "-->" + absoluteAddress);
			playList.put(date + " " + title, absoluteAddress);
		}
		return playList;
	}

	public static void main(String[] args) {

		String saveRootpath = "D:\\Workspace\\Temporary\\QQMusicTools";
		String introduceDir = "D:\\Workspace\\Temporary\\QQMusicTools\\Introduce";
		QQMusicPlaylistLoader tool = new QQMusicPlaylistLoader(saveRootpath, introduceDir);
		tool.createPlayListDirectory();
	}
}
