package javasking.picture.avmoo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * AVMOO图片下载器。
 * 
 * @author ShijinLin
 *
 * 2016年11月20日
 */
public class AVMOLoader{
	
	public static final String LOGGER_DIRECTORY = "D:\\Workspace\\Eclipse\\WorkspaceLog";//日志目录
	
	public static void main(String[] args) {
		
		AVMOOMetaData[] AVMOOmetaDatas = new AVMOOMetaData[] { //元信息列表

//				new AVMOOMetaData("Fetish Hood",//81 √
//						new Pair[]{
//								new Pair("https://avso.pw/cn/search/FH",3)
//				},
//						new Pair[]{
//				}),
//				new AVMOOMetaData("あずみ恋 Misaki Ren",//37 + 202 = 239
//						new Pair[]{
//						        new Pair("https://avso.pw/cn/star/3vv",2)
//				},
//						new Pair[]{
//						        new Pair("https://avmo.pw/cn/star/78y",7)
//				}),
//				new AVMOOMetaData("Julia",//607 √
//						new Pair[]{
//				},
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/2de",21)
//				}), 
//				new AVMOOMetaData("愛加あみ Ami Manaka",//5 + 22 = 27 √
//						new Pair[]{
//								new Pair("https://avso.pw/cn/star/7c8",1)
//				},              
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/naw",1)
//				}), 
//				new AVMOOMetaData("西尾かおり Nishio Kaori",//13 + 92 = 105 √
//						new Pair[]{
//								new Pair("https://avso.pw/cn/star/4ke",1)
//				},              
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/2qp",4)
//				}), 
//				new AVMOOMetaData("桜井あゆ 加藤はるき Ayu Sakurai",//561 + 2 = 563 √
//						new Pair[]{
//				},              
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/91l",19),
//								new Pair("https://avmo.pw/cn/star/9p2",1)
//				}), 
//				new AVMOOMetaData("大橋未久 Ohashi Miku",//33 + 389 = 422 √
//						new Pair[]{
//								new Pair("https://avso.pw/cn/star/78x",2)
//				},              
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/2zz",13)
//				}), 
//				new AVMOOMetaData("管野しずか Shizuka Kanno 神納花 Kano Hana",//434 + 62 = 496 √
//						new Pair[]{
//				},              
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/26p",15),
//								new Pair("https://avmo.pw/cn/star/pwc",3),
//				}), 
//				new AVMOOMetaData("白木優子 Shiraki Yuuko",//0 + 75 = 75 √
//						new Pair[]{
//				},              
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/80i",3)
//				}), 
				new AVMOOMetaData("めぐり 藤浦めぐ Meguri",//290
						new Pair[]{
				},              
						new Pair[]{
								new Pair("https://avmo.pw/cn/star/305",10)
				}), 
//				new AVMOOMetaData("愛原エレナ Aihara Erena",//17 + 7 = 24 √
//						new Pair[]{
//								new Pair("https://avso.pw/cn/star/3m7",1)
//				},              
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/2h2",1)
//				}), 
//				new AVMOOMetaData("愛原つばさ Aihara Tsubasa",//18 + 61 = 79 √
//						new Pair[]{
//								new Pair("https://avso.pw/cn/star/2yx",1)
//				},              
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/57c",3)
//				}), 
//				new AVMOOMetaData("北川エリカ 園田ゆりあ Kitagawa Erika",//647 + 1 = 648 √
//						new Pair[]{
//				},              
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/6vo",22),
//								new Pair("https://avmo.pw/cn/star/l0v",1),
//				}), 
//				new AVMOOMetaData("本澤朋美 芹沢さくら 枝村千春 Motozawa Tomomi",//19 + 1 + 16 + 42 = 78 √
//						new Pair[]{
//								new Pair("https://avso.pw/cn/star/6wo",1)
//				},              
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/edm",2),
//								new Pair("https://avmo.pw/cn/star/fn3",1),
//								new Pair("https://avmo.pw/cn/star/9qd",1),
//				}), 
//				new AVMOOMetaData("西条麗 Saijo Rei",//3 + 90 =93 √
//						new Pair[]{
//								new Pair("https://avso.pw/cn/star/ak5",1)
//				},              
//						new Pair[]{
//								new Pair("https://avmo.pw/cn/star/1v7",3),
//				}), 				
				new AVMOOMetaData("村上里沙 真宮梨沙子 Risako Mamiya",//14 + 7 + 209 = 230
						new Pair[]{
								new Pair("https://avso.pw/cn/star/2d5",1),
								new Pair("https://avso.pw/cn/star/4oy",1),
				},              
						new Pair[]{
								new Pair("https://avmo.pw/cn/star/1tx",7),
				}), 				

				// new AVMOOMetaData("桜ちなみ","https://avmo.pw/cn/star/oi8",4),     
				// new AVMOOMetaData("原紗央莉","https://avmo.pw/cn/star/61t",2),
				// new AVMOOMetaData("めぐり（藤浦めぐ）","https://avmo.pw/cn/star/305",10),
				// new AVMOOMetaData("希崎ジェシカ","https://avmo.pw/cn/star/5kp",10),
				// new AVMOOMetaData("椎名ゆな","https://avmo.pw/cn/star/2su",21),
				// new AVMOOMetaData("紫彩乃","https://avmo.pw/cn/star/in",16),
				// new AVMOOMetaData("椎名まりな","https://avmo.pw/cn/star/93p",2),
				// new AVMOOMetaData("桜木凛","https://avmo.pw/cn/star/5ek",9),
				// new AVMOOMetaData("由愛可奈","https://avmo.pw/cn/star/2lb",6),
				// new AVMOOMetaData("有村千佳", "https://avmo.pw/cn/star/2p8", 30),
				// new AVMOOMetaData("志村玲子", "https://avmo.pw/cn/star/1ci", 11),
				// new AVMOOMetaData("一条綺美香","https://avmo.pw/cn/star/88g",2),
				// new AVMOOMetaData("香西咲","https://avmo.pw/cn/star/7i6",4),
				// new AVMOOMetaData("今井真由美","https://avmo.pw/cn/star/plm",1),
				// new AVMOOMetaData("井上綾子","https://avmo.pw/cn/star/naq",4),
				// new AVMOOMetaData("羽生ありさ","https://avmo.pw/cn/star/q68",1),
				// new AVMOOMetaData("青山葵","https://avmo.pw/cn/star/7sm",4),
		};
		
		String rootPath = "D:\\DownLoad\\RH";//图片保存根目录

		AVMOLoader loader = new AVMOLoader();//创建下载器
		
		for (int i = 1; i <= AVMOOmetaDatas.length; i++) {
			
			String savePath = rootPath + File.separator + AVMOOmetaDatas[i-1].getIdol();
			System.out.println("\n" + "Root directory :  " + savePath + "\n");
			/* 1、创建根目录 */
			if(!new File(savePath).exists()) new File(savePath).mkdirs();
			/* 2、创建步兵作品，骑兵作品目录 */
			String bubingDir = savePath + File.separator + "步兵 " + AVMOOmetaDatas[i-1].getIdol();
			if(!new File(bubingDir).exists()) new File(bubingDir).mkdirs();
			String qibingDir = savePath + File.separator + "骑兵 " + AVMOOmetaDatas[i-1].getIdol();
			if(!new File(qibingDir).exists()) new File(qibingDir).mkdirs();
			/* 3、创建单体合集子目录 */
			String dantiBubing = bubingDir + File.separator + "单体";
			String hejiBubing = bubingDir + File.separator + "合集";
			String dantiQibing = qibingDir + File.separator + "单体";
			String hejiQibing = qibingDir + File.separator + "合集";
			if(!new File(dantiBubing).exists()) new File(dantiBubing).mkdirs();
			if(!new File(hejiBubing).exists()) new File(hejiBubing).mkdirs();
			if(!new File(dantiQibing).exists()) new File(dantiQibing).mkdirs();
			if(!new File(hejiQibing).exists()) new File(hejiQibing).mkdirs();
			/* 4、分别下载步兵骑兵番号 */ 
			Pair[] BUBING = AVMOOmetaDatas[i - 1].getBubing();
			for(int j = 1;j <= BUBING.length;j++) {
				for (int k = 1; k <= BUBING[j - 1].getLength(); k++) {
					loader.LoaderPicture(BUBING[j-1].createNextPageURL(), bubingDir);
				}
			}
			Pair[] QIBING = AVMOOmetaDatas[i - 1].getQibing();
			for(int j = 1;j <= QIBING.length; j++) {
				for (int k = 1; k <= QIBING[j - 1].getLength(); k++) {
					loader.LoaderPicture(QIBING[j-1].createNextPageURL(), qibingDir);
				}
			}
		}
	}
	
	/**
	 * 错误日志，用于恢复机制。
	 */
	private StringBuffer errorLogger = new StringBuffer();
	
	/**
	 * 未完成列表。保存格式：下载图片URL 保存路径
	 */
	private Map<String, String> UNDONE = new HashMap<String, String>();
	

	/**
	 * 从起始页面跳转到子页面下载图片到指定目录。
	 * 
	 * @param pageURL 起始页面。
	 * 
	 * @param savePath 图片保存根路径。
	 */
	public void LoaderPicture(String pageURL, String savePath){
		
		/* 1、获取起始页面源码 */
		System.out.println("\n" + "Loading the source of root page : " + pageURL + "\n");
		String pageContent = getHtmlContentByURL(pageURL);
		if(pageContent == null) {
			System.err.println("\n" + "Get the source of root page[" + pageURL + "] occur error!" + "\n");
			errorLogger.append("\n" + "Get the source of root page[" + pageURL + "] occur error!" + "\n");
			return;
		}
		//System.out.println("\n" + "The source of root page is :\n " + pageContent + "\n");
		/* 2、根据网站图片排版模式，解析获取所有子页面路径 */
		System.out.println("\n" + "Extracting the children pages from root page!" + "\n");
		String[] childPageURLS = extractChildPageURLS(pageURL, pageContent);
		/* 3、依次访问子页面下载图片并保存 */
		if(childPageURLS != null) {
			for(String childpageURL : childPageURLS) {
				System.out.println("\n" + "Downloading from the children page : [" + childpageURL + "]\n");
				downLoadPhotosetFromURL(childpageURL, createDirectory(savePath));
			}
		}
		/* 4、继续未完成下载 */
		recover(savePath);
	}
	
	/**
	 * 根据网站图片排版模式，解析获取所有子页面路径。
	 * 
	 * @param pageURL 起始页面URL。
	 * 
	 * @param pageContent 起始页面源码。
	 * 
	 * @return 子页面路径列表。
	 */
	public String[] extractChildPageURLS(String pageURL, String pageContent) {
		
		String Hpattern = "href=\"";
		if (pageContent == null || pageContent.isEmpty()) {
		    System.err.println("\n" + "Get the source of children page[" + pageURL + "] occur error!" + "\n");
		    errorLogger.append("\n" + "Get the source of children page[" + pageURL + "] occur error!" + "\n");
			return new String[0];
		}
		String pattern = "<a class=\"movie-box";
		String[] parts = pageContent.split(pattern, -1);
		String[] result = new String[parts.length - 1];
		try{
			for (int i = 1; i < parts.length; i++) {
				int start = parts[i].indexOf(Hpattern);//找第一个href标记
				int end = parts[i].indexOf("\"", start + Hpattern.length() + 1);
				result[i - 1] = parts[i].substring(start + Hpattern.length(), end);
				System.out.println(result[i - 1]);//打印子页面URL列表
			}
		}catch(Exception ex) {
			System.err.println("\n Extracting the children pages of [" + pageURL + "] occur error!\n");
			errorLogger.append("\n Extracting the children pages of [" + pageURL + "] occur error!\n");
		}
		return result;
	}
	
	public String createDirectory(String savePath) {
		
		return savePath;//这里没有分目录操作，所以直接返回
	}

	/**
	 * 从网页提取照片名并下载保存照片。
	 * 
	 * @param pageURL 网页URL。
	 * 
	 * @param savePath 照片保存根目录。
	 */
	public void downLoadPhotosetFromURL(String pageURL, String savePath) {
		
		System.out.println("\n" + "Loading the source of children page : [" + pageURL + "]\n");
		String childPageContent = getHtmlContentByURL(pageURL);
		//System.out.println("\n" + "The source of the children page : \n" + childPageContent + "\n");
		if(childPageContent == null) {
			System.err.println("\n Extracting the title of image from  [" + pageURL + "] occur error! Not a picture save to [" + savePath + "]\n");
			errorLogger.append("\n Extracting the title of image from  [" + pageURL + "] occur error! Not a picture save to [" + savePath + "]\n");
			return;
		}
		String AVATAR = "<a class=\"avatar-box\"";//演员列表，用于判断是否为单体作品
		String BIGIMAGE = "<a class=\"bigImage\"";
		String IMGSRC = "<img src=\"";
		String HEADER = "<span class=\"header\">识别码:</span>";
		String STYLE = "<span style=\"color:#CC0000;\">";
		/* 提取文件名和下载路径 */
		String filename = "";
		String pictureURL = "";
		try{
			int BIGIMAGEINDEX = childPageContent.indexOf(BIGIMAGE);
			int urlstart = childPageContent.indexOf(IMGSRC, BIGIMAGEINDEX);
			int  urlend = childPageContent.indexOf("\"", urlstart + IMGSRC.length() + 1);
			pictureURL = childPageContent.substring(urlstart + IMGSRC.length(), urlend);
			int HEADERINDEX = childPageContent.indexOf(HEADER);
			int namestart  = childPageContent.indexOf(STYLE,HEADERINDEX);
			int nameend = childPageContent.indexOf("<", namestart + STYLE.length() + 1);
			String biaoshi = childPageContent.substring(namestart + STYLE.length(), nameend);
			/* 将单体作品和合集作品分开保存 */
			if(childPageContent.split(AVATAR).length == 2) { //演员只有一个为单体作品
				filename = savePath + File.separator + "单体"  + File.separator + biaoshi + ".jpg";
			}else{
				filename = savePath + File.separator + "合集" + File.separator + biaoshi + ".jpg";
			}
		}catch(Exception ex) {
			System.err.println("\n Extracting the title of image from  [" + pageURL + "] occur error! Not a picture save to [" + savePath + "]\n");
			errorLogger.append("\n Extracting the title of image from  [" + pageURL + "] occur error! Not a picture save to [" + savePath + "]\n");
		}
		downLoadBigImageFromURL(pictureURL, filename);//开始下载图片
	}
	
	/**
	 * 下载图片。
	 * 
	 * @param pictureURL 图片URL。
	 * 
	 * @param filename 保存文件名。
	 */
	public void downLoadBigImageFromURL(String pictureURL, String filename) {
		
		if(new File(filename).exists()) {
			System.out.println("\nThe image : [" + pictureURL + "] is already loaded to local [" + filename + "]!\n");
			return ;/* 文件已经存在，直接返回 */
		}
		try{
			URL url = new URL(pictureURL);
			System.out.println("\nLoading the image : " + pictureURL + "\n");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.setReadTimeout(10 * 1000);
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
			InputStream inputStream = conn.getInputStream();
			byte[] getData = readInputStream(inputStream);
			System.out.println("\nSaving the image from [" + pictureURL + "] to local direction [" + filename + "]\n");
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(getData);
			if (inputStream != null) {
				inputStream.close();
			}
			if (fos != null) {
				fos.close();
			}
			conn.disconnect();			
		}catch(IOException ex){
			System.err.println("\nSaving the image from [" + pictureURL + "] to local direction [" + filename + "] occur error!\n");
			errorLogger.append("\nSaving the image from [" + pictureURL + "] to local direction [" + filename + "] occur error!\n");
			UNDONE.put(pictureURL, filename);//保存未完成列表
		}
		
	}

	/**
	 * 完成中断的下载任务。
	 * 
	 * @param savePath 照片保存根目录。
	 */
	public void recover(String savePath) {
		
		/* 1、写日志文件 */
		String logfileName = savePath.substring(savePath.lastIndexOf(File.separator) + 1);
		try{
			File logFile = new File(LOGGER_DIRECTORY + File.separator + logfileName + ".txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
			writer.write(errorLogger.toString());
			if(writer != null) {
				writer.close();
			}
		}catch(Exception e) {
			System.err.println("\n Error Write the log file!\n");
		}
		continueUndone();//完成未下载任务
		errorLogger = new StringBuffer();//清空日志
		UNDONE.clear();//清空未完成列表
	}
	
	/**
	 * 继续未完成下载
	 */
	public void continueUndone() {
		
		System.out.println("\n Loading the undone image!\n");
		/* 2、尝试完成未下载的文件 */
		for(String undoneURL : UNDONE.keySet()) {
			try{
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
			}catch(Exception e) {
				System.err.println("\nSaving the image from [" + undoneURL + "] to local direction [" + UNDONE.get(undoneURL) + "] occur error!\n");
			}
		}
	}
	
	/* 其他工具方法 */
	
	/**
	 * 获取指定网页源码，大小写敏感。
	 * 
	 * @param URLString 网页URL。
	 * 
	 * @return 网页源码。
	 */
	private String getHtmlContentByURL(String URLString) {
		
		return getHtmlContentByURL(URLString, true);
	}
	
	/**
	 * 获取指定网页的源码。
	 * 
	 * @param URLString 网页URL。
	 * 
	 * @param isCase 是否大小写敏感。
	 * 
	 * @return 网页源码。
	 */
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
	
	/**
	 * 从指定字节流中读取字节。
	 * 
	 * @param inputStream 待读取字节流。
	 * 
	 * @return 读取的字节数组。
	 * 
	 * @throws IOException IO异常。
	 */
	public static  byte[] readInputStream(InputStream inputStream) throws IOException {
		
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
 * AVMOO网站元信息。
 * 
 * @author ShijinLin
 *
 * 2016年12月20日
 */
class AVMOOMetaData{
	
	String IDOL;//IDOL名称（图片保存根目录名）
	
	Pair[] BUBING;//步兵元信息列表
	
	Pair[] QIBING;//骑兵元信息列表
	
	/**
	 * 默认构造函数。
	 * 
	 * @param IDOL IDOL名称。
	 * 
	 * @param BUBING 步兵元信息列表。
	 * 
	 * @param QIBING 骑兵元信息列表。
	 */
	public AVMOOMetaData(String IDOL, Pair[] BUBING, Pair[] QIBING) {
		
		this.IDOL = IDOL;
		this.BUBING = BUBING;
		this.QIBING = QIBING;
	}
	
	/**
	 * 获取步兵元信息列表。
	 * 
	 * @return 步兵元信息列表。
	 */
	public Pair[] getBubing() {
		
		return this.BUBING;
	}
	
	/**
	 * 获取骑兵元信息列表。
	 * 
	 * @return 骑兵元信息列表。
	 */
	public Pair[] getQibing() {
		
		return this.QIBING;
	}
	
	/**
	 * 获取IDOL名称。
	 * 
	 * @return IDOL名称。
	 */
	public String getIdol() {
		
		return this.IDOL;
	}
}

/**
 * 起始URL和访问深度对。
 * 
 * @author ShijinLin
 *
 * 2016年12月20日
 */
class Pair {
	
	String STARTURL;//起始URL 
	
	int LENGTH;//访问深度
	
	private int index = 1;//URL索引
	
	private static final String PAGE = "page";//用于拼接URL
	
	/**
	 * 默认构造函数。
	 * 
	 * @param STARTURL 起始URL。
	 * 
	 * @param LENGTH 访问深度。
	 */
	public Pair(String STARTURL, int LENGTH) {
		
		this.STARTURL = STARTURL;
		this.LENGTH = LENGTH;
	}
	
	/**
	 * 获取起始URL。
	 * 
	 * @return 起始URL。
	 */
	public String getURL() {
		
		return this.STARTURL;
	}
	
	/**
	 * 获取访问深度。
	 * 
	 * @return 访问深度。
	 */
	public int getLength() {
		
		return this.LENGTH;
	}
	
	/**
	 * 获取下个待访问URL。
	 * 
	 * @return 下个待访问URL。
	 */
	public String createNextPageURL() {
		
		String nextPageURL = getURL() + "/" + PAGE + "/" + index++;
		return nextPageURL;
	}
}

