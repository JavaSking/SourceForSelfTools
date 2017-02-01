package javasking.picture.avmoo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据日志启动恢复机制。
 * 
 * @author ShijinLin
 *
 * 2016年12月9日
 */
public class AVMOLoaderRecoverFromLogger {
	
	public static void main(String[] args) {
		
		AVMOLoaderRecoverFromLogger recover = new AVMOLoaderRecoverFromLogger();
		String file = "D:\\Workspace\\Eclipse\\WorkspaceLog\\recover\\XXXXXX.txt";
		recover.recover(file);
	}
	
	/* 用于标记待恢复动作 */
	
	public static final String IMAGE_KEY = "IMAGE_KEY";//恢复下载一张图片（已完成从网页提取照片名）
	
	public static final String IMAGE_TITLE_KEY = "IMAGE_TITLE_KEY";//恢复下载一张图片（未完成从网页提取照片名）
	
	public static final AVMOLoader LOADER = new AVMOLoader();//下载器
	
	public static final String IMAGE_TAG = "Saving the image from ";
	
	public static final String IMAGE_TITLE_TAG = " Extracting the title of image from  ";
	
	public void recover(String logFile) {
		
		File logger = new File(logFile);
		if(!logger.exists() || logger.isDirectory()) {
			System.err.println("\n The file :" + logFile + " is not exists or is not a file! return to do anything!\n");
			return;
		}
		Map<String, List<String>> recoverRecords = readLineFromLogFile(logger);
		for(String key : recoverRecords.keySet()) {
			List<String> imageRecords = recoverRecords.get(key);
			if(key.equals(IMAGE_KEY)) {
				for(String imageRecord : imageRecords) {
					/* 提取图片URL和保存路径 */
					int urlstart = IMAGE_TAG.length() + 1;
					int urlend = imageRecord.indexOf("]", urlstart);
					String bigImageURL = imageRecord.substring(urlstart, urlend);
					int savefilestart = imageRecord.indexOf("[", urlend);
					int savefileend = imageRecord.lastIndexOf("]");
					String savefile = imageRecord.substring(savefilestart + 1, savefileend);
					downLoadBigImageFromURL(bigImageURL, savefile);
				}
			}else if(key.equals(IMAGE_TITLE_KEY)) {
				for(String imageRecord : imageRecords) {
					int urlstart = IMAGE_TITLE_TAG.length() + 1;
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
	 * 从子页面中下载图片保存到指定路径。
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
	public void downLoadBigImageFromURL(String bigImageURL, String savefile) {
		
		LOADER.downLoadBigImageFromURL(bigImageURL, savefile);
	}
	
	/**
	 * 从日志中读取行。日志中每一行保存一个待恢复记录。
	 * 
	 * @param logger 日志文件。
	 * 
	 * @return 带恢复记录。
	 */
	public Map<String, List<String>>readLineFromLogFile(File logger) {
		
		String line = "";
		BufferedReader reader = null;
		List<String> IMAGE = new ArrayList<String>();
		List<String> PHOTOSET = new ArrayList<String>();
		Map<String, List<String>> recoverRecords = new HashMap<String, List<String>>();
		try{
			reader = new BufferedReader(new FileReader(logger));
			while((line = reader.readLine()) != null) {
				if(line.startsWith(IMAGE_TAG)){
					IMAGE.add(line);
				}else if(line.startsWith(IMAGE_TITLE_TAG)){
					PHOTOSET.add(line);
				}
			}
		}catch(Exception e) {
			System.err.println("\n Reading the logger occur error!");
			e.printStackTrace();
		}
		recoverRecords.put(IMAGE_KEY, IMAGE);
		recoverRecords.put(IMAGE_TITLE_KEY, PHOTOSET);
		return recoverRecords;//待恢复记录，key：待恢复动作标识 value：待恢复动作列表
	}
}

