package javasking.picture.data18;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 18Data.com专辑下载器。
 * 
 * @author JavaSking 2017年1月28日
 */
public class Data18Loader implements Runnable {
	
	// http://www.data18.com/sys/pass.php?ip=116.23.65.162&ref=/tanya_tate/filmography/

	/**
	 * 保存根目录。
	 */
	public static final String rootPath = "D:\\DownLoad";

	/**
	 * 日期标签。
	 */
	private static final String DATE = "<p class=\"genmed\" style=\"text-align: left;\"><b>#";

	/**
	 * 封面标题。
	 */
	private static final String TITLE = "<h1 style=\"height: 23px; overflow: hidden; font-size: 18px; margin: 3px;\">";

	private static final String SERIE = "<b>Serie:</b>";

	private static final String SITE = "<b>Site:</b>";

	private static final String STUDIO = "<b>Studio:</b>";
	
	private static final String NETWORK = "<b>Network:</b>";
	
	private static final String B = "</b>";
	
	private static final String P = "</p>";
	
	private static final String HREF = "<a href=\"";
	
	private static final String QUOTE = "\"";
	
	private static final String COVER = "class=\"grouped_elements\"";
	
	private static final String COVERURL = "<img src=\"";
	
	private static final String SCENE = "<span class=\"gen\">";
	
	private static final String SPAN = "</span>";
	
	private static final String GEN12 = "<p class=\"gen12\">&nbsp;Starring: ";
	
	private static final String RIGHT = "\">";
	
	private static final String A = "</a>";
	
	private static final String TIME = "<b>Movie Length</b>:";
	
	private static final String SCENECOVER = "<div style=\"float: left; margin: 8px; width: 600px; overflow: hidden;\">";
	
	private static final String SCENECOVERURL = "<div id=\"moviewrap\" style=\"position: relative; width: 640; height: 335px; overflow: hidden;\">";
	
	private static final String IMAGESRC = "<img src=\"";
	

	/**
	 * 元信息。
	 */
	private DATA18MetaData metaData;
	
	/**
	 * 未完成列表。保存格式：下载图片URL 保存路径
	 */
	private Map<String, String> UNDONE = new HashMap<String, String>();

	/**
	 * 构造方法。
	 * 
	 * @param metaData 元信息。
	 */
	public Data18Loader(DATA18MetaData metaData) {

		this.metaData = metaData;
	}

	/**
	 * 默认构造方法。
	 */
	public Data18Loader() {

	}

	/**
	 * 获取元信息。
	 * 
	 * @return 元信息。
	 */
	public DATA18MetaData getMetaData() {

		return this.metaData;
	}

	public static void main(String[] args) {
		
		DATA18MetaData metaData = new DATA18MetaData("Tanya Tate", "http://www.data18.com/tanya_tate/filmography/", 7);
		new Thread(new Data18Loader(metaData)).start();

	}

	public void run() {

		String savePath = rootPath + File.separator + getMetaData().getName();
		System.out.println("\n" + "Save directory :  " + savePath + "\n");
		/* 创建目录 */
		new File(savePath).mkdir();
		for (int j = 1; j <= getMetaData().getLength(); j++) {
			//System.out.println(getMetaData().createNextPageURL());
			LoaderPicture(getMetaData().createNextPageURL(), savePath);
		}
	}
	
	public void LoaderPicture(String pageURL, String savePath){
		
		System.out.println("\n" + "Loading the source of root page : " + pageURL + "\n");
		String pageContent = getHtmlContentByURL(pageURL);
		Map<String, String> childPageURLS = extractChildPageURLS(pageURL, pageContent);
		if(childPageURLS.size() > 0) {
			for(String childpageURL : childPageURLS.keySet()) {
				System.out.println("\n" + "Downloading from the children page : [" + childpageURL + "]\n");
				LoaderInformation(childpageURL, childPageURLS.get(childpageURL), savePath);
			}
		}
		continueUndone();
	}
	
	public void LoaderInformation(String pageURL, String date, String savePath) {
		
		System.out.println("\n" + "Loading the source of page : " + pageURL + "\n");
		String pageContent = getHtmlContentByURL(pageURL);
		System.out.println("Date:	[" + date + "]");
		/* 提取标题 */
		int titleStartIndex = pageContent.indexOf(TITLE);
		int titleEndIndex = pageContent.indexOf("<", titleStartIndex + TITLE.length() + 1);
		String title = pageContent.substring(titleStartIndex + TITLE.length(), titleEndIndex);
		title = title.replace(':', '-').replace('/', '-'); 
		System.out.println("Title:	[" + title + "]");
		/* 提取系列 */
		String series = getSeries(pageContent);
		System.out.println("Series:	[" + series + "]");
		/* 提取封面 */
		int coverStartIndex = pageContent.indexOf(COVER);
		int coverURLStartIndex = pageContent.indexOf(COVERURL, coverStartIndex + COVER.length() + 1);
		int coverURLEndIndex = pageContent.indexOf(QUOTE, coverURLStartIndex + COVERURL.length() + 1);
		String coverURL = pageContent.substring(coverURLStartIndex + COVERURL.length(), coverURLEndIndex);
		System.out.println("CoverURL:	[" + coverURL + "]");
		/* 提取时长 */
		String time = "";
		if(pageContent.contains(TIME)) {
			int timeStartIndex = pageContent.indexOf(TIME);
			int timeEndIndex = pageContent.indexOf("<a", timeStartIndex + TIME.length() + 1);
			String tempTime = pageContent.substring(timeStartIndex + TIME.length(), timeEndIndex);
			time = formatTime(tempTime);
		}
		System.out.println("Time:	[" + time + "]");
		/* 创建子目录 */
		String subDirectory =  "[" + series + "]" + title + " " + date + " " + time + " min";
		new File(savePath + File.separator + subDirectory).mkdirs();
		/* 保存封面 */
		String cover = savePath + File.separator + subDirectory + File.separator + subDirectory + ".jpg";
		downLoadCover(coverURL, cover);
		String[] parts = pageContent.split(SCENE, -1);
		try{
			for (int i = 1; i < parts.length; i++) {
				/* 获取Scene */
				int sceneStartIndex = parts[i].indexOf("<b>");
				int sceneEndIndex = parts[i].indexOf(B, sceneStartIndex + "<b>".length() + 1);
				String scene = parts[i].substring(sceneStartIndex + "<b>".length(), sceneEndIndex).trim();
				System.out.println("Scene:	[" + scene + "]");
				/* 提取时长 */
				int durationStartIndex = parts[i].indexOf(SPAN);
				int durationEndIndex = parts[i].indexOf("</p>", durationStartIndex + SPAN.length() + 1);
				String duration = parts[i].substring(durationStartIndex + SPAN.length(),  durationEndIndex);
				System.out.println("Duration:	[" + getDuration(duration) + "]");
				/* 提取Scene图片 */
				int sceneCoverIndex = parts[i].indexOf(SCENECOVER);
				int sceneCoverStartIndex = parts[i].indexOf(HREF, sceneCoverIndex);
				int sceneCoverEndIndex = parts[i].indexOf(QUOTE, sceneCoverStartIndex + HREF.length() + 1);
				String sceneCoverURL = parts[i].substring(sceneCoverStartIndex + HREF.length(), sceneCoverEndIndex);
				System.out.println("sceneCoverURL:	[" + sceneCoverURL + "]");
				/* 提取影星 */
				int gen12StartIndex = parts[i].indexOf(GEN12);
				int gen12EndIndex = parts[i].indexOf(P, gen12StartIndex + GEN12.length() + 1);
				String Starring = parts[i].substring(gen12StartIndex + GEN12.length(), gen12EndIndex);
				String[] starParts = Starring.split(HREF, -1);
				for (int j = 1; j < starParts.length; j++) {
					int starStartIndex = starParts[j].indexOf(RIGHT);
					int starEndIndex = starParts[j].indexOf(A);
					String star = starParts[j].substring(starStartIndex + RIGHT.length(), starEndIndex);
					System.out.println("star:	[" + star + "]");
					/* 包含目标明星的Scene */
					if(star.equalsIgnoreCase(getMetaData().getName())) {
						String sceneDirectoryName = "[" + series + "]" + title + " " + date + " " + scene + getDuration(duration);
						/* 创建Scene目录 */
						String sceneDirectory =  savePath + File.separator + subDirectory + File.separator + sceneDirectoryName;
						new File(sceneDirectory).mkdirs();
						/* 下载Scene封面图片 */
						String scenePageContent = getHtmlContentByURL(sceneCoverURL);
						int sceneCoverURLIndex = scenePageContent.indexOf(SCENECOVERURL);
						int sceneCoverURLStartIndex = scenePageContent.indexOf(IMAGESRC, sceneCoverURLIndex);
						int sceneCoverURLEndIndex = scenePageContent.indexOf(QUOTE, sceneCoverURLStartIndex + IMAGESRC.length() + 1);
						String sceneCoverPicURL = scenePageContent.substring(sceneCoverURLStartIndex + IMAGESRC.length(), sceneCoverURLEndIndex);
						downLoadCover(sceneCoverPicURL, sceneDirectory + File.separator + sceneDirectoryName + ".jpg");
					}
				}
			}
		}catch (Exception ex) {
			System.err.println("\n Extracting the Scene of [" + pageURL + "] occur error!\n");
		}
	}
	
	/**
	 * 格式化时间。
	 * 
	 * @param tempTime
	 * @return
	 */
	private String formatTime(String tempTime){
		
		if(tempTime.contains("min")) {
			return tempTime.substring(0, tempTime.indexOf("min")).trim();
		}else{
			String temp = tempTime.substring(0, 9).trim();
			int hour = Integer.parseInt(temp.substring(1, 2));
			int minute = Integer.parseInt(temp.substring(3, 5));
			return (hour * 60 + minute) + "";
		}
	}
	
	private void downLoadCover(String coverURL, String savePath) {
		
		if(new File(savePath).exists()) {
			return ;
		}
		try {
			URL url = new URL(coverURL);
			System.out.println("\nLoading the cover : " + coverURL + "\n");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.setReadTimeout(10 * 1000);
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
			InputStream inputStream = conn.getInputStream();
			byte[] getData = readInputStream(inputStream);
			System.out.println("\nSaving the cover from [" + coverURL + "] to local direction [" + savePath + "]\n");
			FileOutputStream fos = new FileOutputStream(savePath);
			fos.write(getData);
			if (inputStream != null) {
				inputStream.close();
			}
			if (fos != null) {
				fos.close();
			}
			conn.disconnect();
		} catch (Exception ex) {
			System.err.println("\nSaving the image from [" + coverURL + "] to local direction [" + savePath + "] occur error!\n");
			UNDONE.put(coverURL, savePath);
		}
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
	
	private String getDuration(String date) {
		
		String temp = date.substring(date.lastIndexOf("&nbsp;") + "&nbsp;".length()).trim().replace(":", "").trim();
		return temp.isEmpty() ? "" : " " + temp;
	}
	
	private String getSeries(String pageContent) {

		int bIndex = -1;
		String TAG = "";
		if(pageContent.contains(NETWORK)) {
			bIndex = pageContent.indexOf(NETWORK);
			TAG = NETWORK;
		} else if (pageContent.contains(SITE)) {
			bIndex = pageContent.indexOf(SITE);
			TAG = SITE;
		} else if (pageContent.contains(STUDIO)) {
			bIndex = pageContent.indexOf(STUDIO);
			TAG = STUDIO;
		} else if (pageContent.contains(SERIE)) {
			bIndex = pageContent.indexOf(SERIE);
			TAG = SERIE;
		}
		int seriesStartIndex = pageContent.indexOf(">", bIndex + TAG.length() + 1);
		int seriesEndIndex = pageContent.indexOf("</a>", seriesStartIndex);
		return pageContent.substring(seriesStartIndex + 1, seriesEndIndex);
	}
	
	
	public Map<String, String> extractChildPageURLS(String pageURL, String pageContent) {

		Map<String, String> result = new HashMap<String, String>();
		if (pageContent == null || pageContent.isEmpty()) {
			return result;
		}
		String[] parts = pageContent.split(DATE, -1);
		try {
			for (int i = 1; i < parts.length; i++) {
				/* 提取日期 */
				int dateStartIndex = parts[i].indexOf(B);
				int dateEndIndex = parts[i].indexOf(P);
				String date = parts[i].substring(dateStartIndex + B.length(), dateEndIndex).trim();
				if(date.length() > 15) {
					date = "PRE-RELEASE";
				}
				/* 提取URL */
				int urlStartIndex = parts[i].indexOf(HREF);
				int urlEndIndex = parts[i].indexOf(QUOTE, urlStartIndex + HREF.length() + 1);
				String url = parts[i].substring(urlStartIndex + HREF.length(), urlEndIndex);
				result.put(url, date);
			}
		} catch (Exception ex) {
			System.err.println("\n Extracting the children pages of [" + pageURL + "] occur error!\n");
		}
		return result;
	}
	
	
	
	
	private String getHtmlContentByURL(String URLString, boolean isCase) {
		
		try{
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
			while ((line = URLinput.readLine()) != null){
				sb.append(line);
			}
			if(in != null) {
				in.close();
			}
			if(URLinput != null) {
				URLinput.close();
			}
			return new String(isCase ? sb.toString() : sb.toString().toUpperCase());
		}catch (Exception e){
			System.out.println("URL : " + URLString + " Error!");
			return  null;
		}
	}

	private String getHtmlContentByURL(String URLString) {
		
		return getHtmlContentByURL(URLString, true);
	}
	
	private byte[] readInputStream(InputStream inputStream) throws IOException {
		
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		while((len = inputStream.read(buffer)) != -1) {  
			bos.write(buffer, 0, len);
		}
		bos.close();
		return bos.toByteArray(); 
	}
}

/**
 * 18DATA网站元信息。
 * 
 * @author JavaSking 2017年1月28日
 */
class DATA18MetaData {

	String NAME;// 图片目录名称

	String STARTURL;// 起始URL

	int LENGTH;// 访问深度
	
	private int index = 1;//URL索引
	
	private static final String PAGE = "page";//用于拼接URL

	public DATA18MetaData(String name, String startUrl, int length) {

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

		if(index == 1) {
			index++;
			return getURL();
		}else{
			return getURL() + PAGE + "_" + (index++) + ".html";
		}
	}
}
