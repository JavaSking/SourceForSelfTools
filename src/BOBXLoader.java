import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BOBX网站图片下载器，包含下载恢复机制。
 * 
 * 支持多线程。
 * 
 * @author JavaSking
 *
 *         2016年12月7日
 */
public class BOBXLoader implements Runnable {

	private int photosetCount;// 照片集数

	public void setPhotosetCount(int photosetCount) {

		this.photosetCount = photosetCount;
	}

	public int getPhotosetCount() {

		return this.photosetCount;
	}

	private BobxMetaData metaData;// 元信息

	public BOBXLoader(BobxMetaData metaData) {

		this.metaData = metaData;
	}

	public BOBXLoader() {

	}

	public BobxMetaData getMetaData() {

		return this.metaData;
	}

	public void run() {

		String rootPath = this.getClass().getResource("/").getPath().substring(1);
		String savePath = rootPath + getMetaData().getName();
		System.out.println("\n" + "Save directory :  " + savePath + "\n");
		for (int j = 1; j <= getMetaData().getLength(); j++) {

			LoaderPicture(getMetaData().createNextPageURL(), savePath);
		}
	}

	public static void main(String[] args) {

		/* 获取当前类所在的目录 */
		String directory = BOBXLoader.class.getResource("/").getPath().substring(1);
		BufferedReader reader = null;
		String bobx = directory + "bobx.txt";// 元信息文件
		List<BobxMetaData> metaDatas = new ArrayList<BobxMetaData>();
		int count = 0;
		try {
			reader = new BufferedReader(new FileReader(new File(bobx)));
			String line;
			while ((line = reader.readLine()) != null) {
				count++;
				int firstIndex = line.indexOf(",");
				int secondIndex = line.indexOf(",", firstIndex + 2);
				String name = line.substring(0, firstIndex).trim();
				String url = line.substring(firstIndex + 1, secondIndex).trim();
				int length = Integer.parseInt(line.substring(secondIndex + 1).trim());
				metaDatas.add(new BobxMetaData(name, url, length));
			}
		} catch (Exception ex) {
			System.err.println("读取文件【" + bobx + "】第【" + count + "】失败!");
		}
		for (BobxMetaData metaData : metaDatas) {
			new Thread(new BOBXLoader(metaData)).start();
		}
	}

	/* 网站根路径 */
	private static final String ROOT = "http://www.bobx.com";

	/**
	 * 错误日志，用于恢复机制。
	 */
	private StringBuffer errorLogger = new StringBuffer();

	/**
	 * 未完成列表。保存格式：下载图片URL 保存路径
	 */
	private Map<String, String> UNDONE = new HashMap<String, String>();

	/**
	 * 下载目标页面图片到指定目录。
	 * 
	 * @param pageURL
	 *          目标页面。
	 * @param savePath
	 *          图片保存路径。
	 */
	public void LoaderPicture(String pageURL, String savePath) {

		/* 1、获取起始页面源码 */
		System.out.println("\n" + "Loading the source of root page : " + pageURL + "\n");
		String pageContent = getHtmlContentByURL(pageURL);
		/* 2、根据不同网站图片排版模式，解析获取所有子页面路径，子类实现 */
		String[] childPageURLS = extractChildPageURLS(pageURL, pageContent);
		setPhotosetCount(childPageURLS.length);// 设置照片集数 用于检验是否下载完毕
		/* 3、依次访问子页面下载图片并保存 */
		if (childPageURLS != null) {
			for (String childpageURL : childPageURLS) {
				System.out.println("\n" + "Downloading from the children page : [" + childpageURL + "]\n");
				downLoadPhotosetFromURL(childpageURL, createDirectory(savePath));
			}
		}
		recover(savePath);// 启动恢复机制
	}

	/**
	 * 解析获取所有子页面路径。
	 * 
	 * @param pageURL
	 *          父页面URL。
	 * 
	 * @param pageContent
	 *          父页面源代码。
	 * 
	 * @return 子页面列表。
	 */
	public String[] extractChildPageURLS(String pageURL, String pageContent) {

		String Hpattern = "HREF=\"";
		String Hpattern2 = "href=\"";
		if (pageContent == null || pageContent.isEmpty()) {
			return new String[0];
		}
		String pattern = "class=\"medblack\"";
		String[] parts = pageContent.split(pattern, -1);
		String[] result = new String[parts.length - 1];
		try {
			for (int i = 1; i < parts.length; i++) {
				int start1 = parts[i].indexOf(Hpattern);
				int start2 = parts[i].indexOf(Hpattern2);
				int start = -1;
				if (start1 > 0) {
					start = start2 > 0 ? (start1 < start2 ? start1 : start2) : start1;
				} else {
					start = start2;
				}
				int end = parts[i].indexOf("\"", start + Hpattern.length() + 1);
				String temp = parts[i].substring(start + Hpattern.length(), end);
				if (temp.startsWith("/")) {
					if (temp.startsWith("/idol")) {
						result[i - 1] = ROOT + temp;
					} else {
						result[i - 1] = pageURL.substring(0, pageURL.length() - 1) + temp;
					}
				} else {
					result[i - 1] = pageURL.substring(0, pageURL.length() - 1) + "/" + temp;
				}
				System.out.println(result[i - 1]);// 打印子页面URL列表
			}
		} catch (Exception ex) {
			System.err.println("\n Extracting the children pages of [" + pageURL + "] occur error!\n");
			errorLogger.append("\n Extracting the children pages of [" + pageURL + "] occur error!\n");
		}
		return result;
	}

	/**
	 * 获取小图页面URL列表。
	 * 
	 * @param pageContent
	 *          小图列表页面源码
	 * 
	 * @param smallImageURLS
	 *          小图页面URL列表。
	 */
	public void collectSmallImageURL(String pageContent, List<String> smallImageURLS) {

		String pattern = "<td colspan=\"2\">";
		String HREF = "HREF=\"";
		String[] parts = pageContent.split(pattern, -1);
		for (int i = 1; i < parts.length; i++) {
			int start = parts[i].indexOf(HREF);
			int end = parts[i].indexOf("\"", start + HREF.length() + 1);
			String temp = parts[i].substring(start + HREF.length(), end);
			smallImageURLS.add(temp.startsWith("/") ? ROOT + temp : ROOT + "/" + temp);
			System.out.println(smallImageURLS.get(smallImageURLS.size() - 1));
		}
		String NEXTTAG = "<link rel=\"next\" href=\"";
		if (pageContent.contains(NEXTTAG)) {
			System.out.println("\nLoading the next page !\n");
			String NEXTPAGEENDTAG = "\">";
			int NEXTTAGINDEX = pageContent.indexOf(NEXTTAG);
			int nextpageend = pageContent.indexOf(NEXTPAGEENDTAG, NEXTTAGINDEX + NEXTTAG.length() + 1);
			String nextpageURL = pageContent.substring(NEXTTAGINDEX + NEXTTAG.length(), nextpageend);
			System.out.println("\nLoading the source of the next page : " + nextpageURL + "\n");
			String nextPageContent = getHtmlContentByURL(nextpageURL);
			collectSmallImageURL(nextPageContent, smallImageURLS);// 递归获取下一页小图页面URL
		}
	}

	public void downLoadPhotosetFromURL(String pageURL, String savePath) {

		String childPageContent = getHtmlContentByURL(pageURL);
		System.out.println(childPageContent);
		/* 0、提取IDOL名称 用于拼装图片保存路径 */
		String IDOL = savePath.substring(savePath.lastIndexOf("/") + 1);
		System.out.println(IDOL);
		/* 1、提取标题和照片数用于创建子目录，按照片集分目录保存 */
		File directory = new File(savePath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String FONTPRETAG = "<font color=\"#ff4747\">";
		String FONTENDTEG = "</font>";
		String BRONZE = "<span class=\"bronze\">@ 0 of ";
		String BRONZE1 = "<span class=\"bronze\">@ ";
		String OF = " of";
		String PIX0 = " pix";
		String PIX = "pix</span>";
		String photoset = "";
		int count = -1;
		try {
			/* 标题 */
			int FONTPRETAGINDEX = childPageContent.indexOf(FONTPRETAG);
			String title = "";
			if (FONTPRETAGINDEX == -1) {// 最后一个照片集
				String BGCOLOR = "<tr bgcolor=\"#332233\"><td><center><span class=\"white\"><b>";
				String BTAGEND = "</b>";
				int idolstart = childPageContent.indexOf(BGCOLOR);
				int idolend = childPageContent.indexOf(BTAGEND, idolstart + BGCOLOR.length() + 1);
				String idol = childPageContent.substring(idolstart + BGCOLOR.length(), idolend);
				title = "Other BOBX collection photos for " + idol;
				int number1start = childPageContent.indexOf(BRONZE1);
				int number1end = childPageContent.indexOf(OF, number1start + BRONZE1.length() + 1);
				int number1 = Integer.parseInt(childPageContent.substring(number1start + BRONZE1.length(), number1end));
				int number2start = number1end + OF.length() + 1;
				int number2end = childPageContent.indexOf(PIX0, number2start);
				int number2 = Integer.parseInt(childPageContent.substring(number2start, number2end));
				count = number2 - number1;
				photoset = title + " [" + (number2 - number1) + "P]";
			} else {
				int titleend = childPageContent.indexOf(FONTENDTEG, FONTPRETAGINDEX + FONTPRETAG.length() + 1);
				title = childPageContent.substring(FONTPRETAGINDEX + FONTPRETAG.length(), titleend);
				/* 照片数 */
				int BRONZEINDEX = childPageContent.indexOf(BRONZE);
				int numberend = childPageContent.indexOf(PIX, BRONZEINDEX + BRONZE.length() + 1);
				String P = childPageContent.substring(BRONZEINDEX + BRONZE.length(), numberend - 1);
				count = Integer.parseInt(P);
				photoset = title + " " + IDOL + " [" + P + "P]";// 组装照片集名称：标题 模特名 [张数]
			}
			if (photoset.contains("<br>")) {
				photoset = photoset.replaceAll("<br>", "");// 处理标题换行BUG
			}
		} catch (Exception ex) {
			System.err.println("\n Extracting the title of photoset from  [" + pageURL + "] occur error! Not a picture save to [" + savePath + "]\n");
			errorLogger.append("\n Extracting the title of photoset from  [" + pageURL + "] occur error! Not a picture save to [" + savePath + "]\n");
		}
		if (photoset.equals(""))
			return;
		System.out.println(photoset);
		File subDirectory = new File(savePath + File.separator + photoset);
		if (!subDirectory.exists()) {
			subDirectory.mkdirs();
		}
		if (subDirectory.listFiles().length == count) {/* 如果照片集已经下载完成则直接返回 */
			return;
		}
		/* 2、搜集小图所在页面，用于链接到大图页面进行下载 */
		List<String> smallImageURLS = new ArrayList<String>();
		try {
			collectSmallImageURL(childPageContent, smallImageURLS);
		} catch (Exception e) {
			System.err.println("\n" + "Collecting ths small image page urls from [" + pageURL + "] occur error!\n");
			errorLogger.append("\n" + "Collecting ths small image page urls from [" + pageURL + "] occur error!\n");
		}
		if (smallImageURLS.isEmpty())
			return;
		int index = 1;// 照片索引
		for (String smallImageURL : smallImageURLS) {
			String filename = photoset + "(" + index++ + ")" + ".jpg";// 保存文件名
			String savefile = savePath + File.separator + photoset + File.separator + filename;// 保存路径
			downLoadImageFromSmallImageURL(smallImageURL, savefile);
		}
	}

	/**
	 * 从小图页面链接到大图页面进行下载。
	 * 
	 * @param smallImageURL
	 *          小图页面。
	 * 
	 * @param savefile
	 *          图片保存路径。
	 */
	public void downLoadImageFromSmallImageURL(String smallImageURL, String savefile) {

		if (new File(savefile).exists()) {
			System.out.println("\nThe big image of small image : [" + smallImageURL + "] is already loaded to local [" + savefile + "]!\n");
			return;/* 文件已经存在，直接返回 */
		}
		String bigImageURL = "";
		String DODOTAG = "dodo(\"";
		String HREF = "HREF=\"";
		String SMALLBLACK = "<A class=\"smallblack\"";
		try {
			String smallImagePageContent = getHtmlContentByURL(smallImageURL);
			int DODOTAGINDEX = smallImagePageContent.indexOf(DODOTAG);
			int bigImageURLstart = smallImagePageContent.indexOf(HREF, DODOTAGINDEX + DODOTAG.length() + 1);
			int bigImageURLend = smallImagePageContent.indexOf("\"", bigImageURLstart + HREF.length() + 1);
			String temp = smallImagePageContent.substring(bigImageURLstart + HREF.length(), bigImageURLend);
			String bigImagePageURL = temp.startsWith("/") ? ROOT + temp : ROOT + "/" + temp;
			String bigImagePageContent = getHtmlContentByURL(bigImagePageURL);
			int SMALLBLACKINDEX = bigImagePageContent.indexOf(SMALLBLACK);
			int bigImagestart = bigImagePageContent.indexOf(HREF, SMALLBLACKINDEX + SMALLBLACK.length() + 1);
			int bigImageend = bigImagePageContent.indexOf("\"", bigImagestart + HREF.length() + 1);
			bigImageURL = ROOT + bigImagePageContent.substring(bigImagestart + HREF.length(), bigImageend);
			System.out.println("\nThe big image url is " + bigImageURL + "\n");
		} catch (Exception e) {
			System.err.println("\n Collecting the big image url of [" + smallImageURL + "] occur error! Not a picture save to [" + savefile + "]\n");
			errorLogger.append("\n Collecting the big image url of [" + smallImageURL + "] occur error! Not a picture save to [" + savefile + "]\n");
		}
		if (!bigImageURL.equals("")) {
			downLoadImageFromBigImageURL(bigImageURL, savefile);
		}
	}

	/**
	 * 下载大图到指定目录。
	 * 
	 * @param bigImageURL
	 *          大图路径。
	 * 
	 * @param savefile
	 *          指定保存目录。
	 */
	public void downLoadImageFromBigImageURL(String bigImageURL, String savefile) {

		if (new File(savefile).exists()) {
			System.out.println("\nThe big image : [" + bigImageURL + "] is already loaded to local [" + savefile + "]!\n");
			return;/* 文件已经存在，直接返回 */
		}
		try {
			URL url = new URL(bigImageURL);
			System.out.println("\nLoading the big image : " + bigImageURL + "\n");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.setReadTimeout(10 * 1000);
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
			InputStream inputStream = conn.getInputStream();
			byte[] getData = readInputStream(inputStream);
			System.out.println("\nSaving the image from [" + bigImageURL + "] to local direction [" + savefile + "]\n");
			FileOutputStream fos = new FileOutputStream(savefile);
			fos.write(getData);
			if (inputStream != null) {
				inputStream.close();
			}
			if (fos != null) {
				fos.close();
			}
			conn.disconnect();
		} catch (Exception ex) {
			System.err.println("\nSaving the image from [" + bigImageURL + "] to local direction [" + savefile + "] occur error!\n");
			errorLogger.append("\nSaving the image from [" + bigImageURL + "] to local direction [" + savefile + "] occur error!\n");
			UNDONE.put(bigImageURL, savefile);// 保存未完成列表
		}
	}

	public String createDirectory(String savePath) {

		return savePath;
	}

	/**
	 * 继续未完成下载
	 */
	public void continueUndone() {

		System.out.println("\n Loading the undone image!\n");
		/* 2、尝试完成未下载的文件 */
		for (String undoneURL : UNDONE.keySet()) {
			try {
				URL url = new URL(undoneURL);
				System.out.println("\nLoading the big image : " + undoneURL + "\n");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(50 * 1000);
				conn.setReadTimeout(50 * 1000);
				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
				InputStream inputStream = conn.getInputStream();
				byte[] getData = readInputStream(inputStream);
				FileOutputStream fos = new FileOutputStream(UNDONE.get(undoneURL));
				fos.write(getData);
				if (inputStream != null) {
					inputStream.close();
				}
				if (fos != null) {
					fos.close();
				}
				conn.disconnect();
			} catch (Exception e) {
				System.err.println("\nSaving the image from [" + undoneURL + "] to local direction [" + UNDONE.get(undoneURL) + "] occur error!\n");
			}
		}
	}

	/**
	 * 恢复机制，记录日志，完成中断的下载任务。
	 */
	public void recover(String savePath) {

		/* 1、写日志文件 */
		String logfileName = savePath.substring(savePath.lastIndexOf(File.separator) + 1);
		try {
			File logFile = new File(savePath + File.separator + logfileName + "日志.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
			writer.write(errorLogger.toString());
			if (writer != null) {
				writer.close();
			}
		} catch (Exception e) {
			System.err.println("\n Error Write the log file!\n");
		}
		continueUndone();// 完成未下载任务
		errorLogger = new StringBuffer();// 清空日志
		UNDONE.clear();// 清空未完成列表
		checkTarget(savePath);
	}

	/**
	 * 检验下载任务是否完成。
	 */
	public void checkTarget(String savePath) {

		String idol = savePath.substring(savePath.lastIndexOf(File.separator) + 1);
		boolean result = true;
		StringBuffer buffer = new StringBuffer();
		/* 1、检查照片集数 */
		File directory = new File(savePath);
		File[] subFiles = directory.listFiles();
		if (subFiles.length != getPhotosetCount()) {
			result = false;
		}
		/* 2、检查各个照片集照片数 */
		for (File subFile : subFiles) {
			String subFileName = subFile.getName();
			buffer.append(subFileName).append("\n");
			int start = subFileName.lastIndexOf("[");
			int end = subFileName.lastIndexOf("P]");
			int count = Integer.parseInt(subFileName.substring(start + 1, end));// 照片集照片数
			if (count != subFile.listFiles().length) {
				result = false;
			}
		}
		if (result) {
			try {
				// 写标志文件
				new File(savePath + File.separator + idol + "_true.txt").createNewFile();
				// 写归档文件
				File guidang = new File(savePath + File.separator + idol + ".txt");
				BufferedWriter writer = new BufferedWriter(new FileWriter(guidang));
				writer.write(buffer.toString());
				if (writer != null) {
					writer.close();
				}
			} catch (Exception ex) {
				System.err.println("\nCreating the flag file [" + savePath + File.separator + idol + "_true.txt" + "] occur error!\n");
			}
		} else {
			try {
				new File(savePath + File.separator + idol + "_false.txt").createNewFile();
			} catch (Exception ex) {
				System.err.println("\nCreating the flag file [" + savePath + File.separator + idol + "_false.txt" + "] occur error!\n");
			}
		}
	}

	private String getHtmlContentByURL(String URLString, boolean isCase) {

		try {
			URL url = new URL(URLString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(false);
			con.setUseCaches(false);
			con.setConnectTimeout(20 * 1000);
			con.setAllowUserInteraction(false);
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
			con.connect();

			StringBuffer sb = new StringBuffer();
			String line = "";
			InputStream in = con.getInputStream();
			BufferedReader URLinput = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			while ((line = URLinput.readLine()) != null) {
				sb.append(line);
			}
			if (in != null) {
				in.close();
			}
			if (URLinput != null) {
				URLinput.close();
			}
			return new String(isCase ? sb.toString() : sb.toString().toUpperCase());
		} catch (Exception e) {
			System.out.println("URL : " + URLString + " Error!");
			return null;
		}
	}

	private String getHtmlContentByURL(String URLString) {

		return getHtmlContentByURL(URLString, true);
	}

	private byte[] readInputStream(InputStream inputStream) throws IOException {

		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		bos.close();
		return bos.toByteArray();
	}

}

class BobxMetaData {

	String NAME;// 图片目录名称

	String STARTURL;// 起始URL

	int LENGTH;// 访问深度

	public BobxMetaData(String name, String startUrl, int length) {

		this.NAME = name;
		this.STARTURL = startUrl;
		this.LENGTH = length;
	}

	public String getName() {

		return this.NAME;
	}

	public String getURL() {

		return this.STARTURL;
	}

	public int getLength() {

		return this.LENGTH;
	}

	public String createNextPageURL() {

		return getURL();
	}
}
