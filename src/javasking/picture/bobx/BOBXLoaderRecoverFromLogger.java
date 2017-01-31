package javasking.picture.bobx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据日志启动恢复机制。支持多线程。
 * 
 * @author ShijinLin
 *
 * 2016年12月9日
 */
public class BOBXLoaderRecoverFromLogger implements Runnable{
	
	private String loggerFile;//恢复日志文件
	
	public BOBXLoaderRecoverFromLogger(String loggerFile) {
		
		this.loggerFile = loggerFile;
	}
	
	public String getLoggerFile() {
		
		return this.loggerFile;
	}
	
	public void run() {
		
		recover();
	}
	
	public static void main(String[] args) {
		
		String[] files = new String[]{
				"D:\\Workspace\\Eclipse\\WorkspaceLog\\recover\\あずみ恋 Misaki Ren.txt",
		};
		for(String file : files) {
			
			new Thread(new BOBXLoaderRecoverFromLogger(file)).start();
		}
	}
	
	/* 用于标记待恢复动作 */
	
	public static final String IMAGE_KEY = "IMAGE_KEY";//恢复一张图片
	
	public static final String BIG_IMAGE_URL_KEY = "BIG_IMAGE_URL_KEY";//恢复从小图链接到大图下载动作
	
	public static final String PHOTOSET_KEY = "PHOTOSET_KEY";//恢复一个照片集动作
	
	public static final BOBXLoader LOADER = new BOBXLoader();//下载器
	
	public static final String IMAGE_TAG = "Saving the image from ";
	
	public static final String BIG_IMAGE_URL_TAG = " Collecting the big image url of ";
	
	public static final String PHOTOSET_TAG = " Extracting the title of photoset from  ";
	
	public void recover() {
		
		File logger = new File(getLoggerFile());
		if(!logger.exists() || logger.isDirectory()) {
			System.err.println("\n The file :" + getLoggerFile() + " is not exists or is not a file! return to do anything!\n");
			return;
		}
		Map<String, List<String>> recoverRecords = readLineFromLogFile(logger);
		for(String key : recoverRecords.keySet()) {
			List<String> imageRecords = recoverRecords.get(key);
			if(key.equals(IMAGE_KEY)) {//恢复下载一张大图
				for(String imageRecord : imageRecords) {
					/* 提取图片URL和保存路径 */
					int urlstart = IMAGE_TAG.length() + 1;
					int urlend = imageRecord.indexOf("]", urlstart);
					String bigImageURL = imageRecord.substring(urlstart, urlend);
					int savefilestart = imageRecord.indexOf("[", urlend);
					int savefileend = imageRecord.lastIndexOf("]");
					String savefile = imageRecord.substring(savefilestart + 1, savefileend);
					downLoadImageFromBigImageURL(bigImageURL, savefile);
				}
			}else if(key.equals(BIG_IMAGE_URL_KEY)) {//恢复从小图页面链接到大图下载
				for(String imageRecord : imageRecords) {
					int urlstart = BIG_IMAGE_URL_TAG.length() + 1;
					int urlend = imageRecord.indexOf("]", urlstart);
					String smallImageURL = imageRecord.substring(urlstart, urlend);
					int savefilestart = imageRecord.indexOf("[", urlend);
					int savefileend = imageRecord.lastIndexOf("]");
					String savefile = imageRecord.substring(savefilestart + 1, savefileend);
					downLoadImageFromSmallImageURL(smallImageURL, savefile);
				}
			}else if(key.equals(PHOTOSET_KEY)) {//恢复下载一个图片集
				for(String imageRecord : imageRecords) {
					int urlstart = PHOTOSET_TAG.length() + 1;
					int urlend = imageRecord.indexOf("]", urlstart);
					String photosetURL = imageRecord.substring(urlstart, urlend);
					int savefilestart = imageRecord.indexOf("[", urlend);
					int savefileend = imageRecord.lastIndexOf("]");
					String savefile = imageRecord.substring(savefilestart + 1, savefileend);
					downLoadPhotosetFromURL(photosetURL, savefile);
				}
			}
		}
		continueUndone();
	}
	
	/**
	 * 完成未下载任务。
	 */
	public void continueUndone() {
		
		LOADER.continueUndone();
	}
	
	/**
	 * 从子页面中下载图片集保存到指定路径。
	 * 
	 * @pageURL 页面路径
	 * 
	 * @param savePath 图片保存路径。
	 */
	public void downLoadPhotosetFromURL(String pageURL, String savePath) {
	
		LOADER.downLoadPhotosetFromURL(pageURL, savePath);
	}
	
	/**
	 * 动作：恢复下载大图到指定目录。
	 * 
	 * @param bigImageURL 待下载图片地址。
	 * 
	 * @param savefile 保存路径。
	 */
	public void downLoadImageFromBigImageURL(String bigImageURL, String savefile) {
		
		LOADER.downLoadImageFromBigImageURL(bigImageURL, savefile);
	}
	
	/**
	 * 动作：恢复从小图页面链接到大图页面进行下载。
	 * 
	 * @param smallImageURL 小图页面。
	 * 
	 * @param savefile 图片保存路径。
	 */
	public void downLoadImageFromSmallImageURL(String smallImageURL, String savefile) {
		
		LOADER.downLoadImageFromSmallImageURL(smallImageURL, savefile);
	}
	
	/**
	 * 从日志中读取行。日志中每一行保存一个待恢复记录。
	 * 
	 * @param logger 日志文件。
	 * @return 带恢复记录。
	 */
	public Map<String, List<String>>readLineFromLogFile(File logger) {
		
		String line = "";
		BufferedReader reader = null;
		List<String> IMAGE = new ArrayList<String>();
		List<String> BIG_IMAGE_URL = new ArrayList<String>();
		List<String> PHOTOSET = new ArrayList<String>();
		Map<String, List<String>> recoverRecords = new HashMap<String, List<String>>();
		try{
			reader = new BufferedReader(new FileReader(logger));
			while((line = reader.readLine()) != null) {
				if(line.startsWith(IMAGE_TAG)){
					IMAGE.add(line);
				}else if(line.startsWith(BIG_IMAGE_URL_TAG)) {
					BIG_IMAGE_URL.add(line);
				}else if(line.startsWith(PHOTOSET_TAG)){
					PHOTOSET.add(line);
				}
			}
		}catch(Exception e) {
			System.err.println("\n Reading the logger occur error!");
			e.printStackTrace();
		}
		recoverRecords.put(IMAGE_KEY, IMAGE);
		recoverRecords.put(PHOTOSET_KEY, PHOTOSET);
		recoverRecords.put(BIG_IMAGE_URL_KEY, BIG_IMAGE_URL);
		return recoverRecords;//待恢复记录，key：待恢复动作标识 value：待恢复动作列表
	}
}
