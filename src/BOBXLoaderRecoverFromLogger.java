import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ������־�����ָ����ơ�֧�ֶ��̡߳�
 * 
 * @author JavaSking
 *
 *         2016��12��9��
 */
public class BOBXLoaderRecoverFromLogger implements Runnable {

	private String loggerFile;// �ָ���־�ļ�

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

		String directory = BOBXLoaderRecoverFromLogger.class.getResource("/").getPath().substring(1);
		String logFileDir = directory + "���ش�����־";
		File[] logFiles = new File(logFileDir).listFiles();
		for (File logFile : logFiles) {
			new Thread(new BOBXLoaderRecoverFromLogger(logFile.getAbsolutePath())).start();
		}
	}

	/* ���ڱ�Ǵ��ָ����� */

	public static final String IMAGE_KEY = "IMAGE_KEY";// �ָ�һ��ͼƬ

	public static final String BIG_IMAGE_URL_KEY = "BIG_IMAGE_URL_KEY";// �ָ���Сͼ���ӵ���ͼ���ض���

	public static final String PHOTOSET_KEY = "PHOTOSET_KEY";// �ָ�һ����Ƭ������

	public static final BOBXLoader LOADER = new BOBXLoader();// ������

	public static final String IMAGE_TAG = "Saving the image from ";

	public static final String BIG_IMAGE_URL_TAG = " Collecting the big image url of ";

	public static final String PHOTOSET_TAG = " Extracting the title of photoset from  ";

	public void recover() {

		File logger = new File(getLoggerFile());
		if (!logger.exists() || logger.isDirectory()) {
			System.err.println("\n The file :" + getLoggerFile() + " is not exists or is not a file! return to do anything!\n");
			return;
		}
		Map<String, List<String>> recoverRecords = readLineFromLogFile(logger);
		for (String key : recoverRecords.keySet()) {
			List<String> imageRecords = recoverRecords.get(key);
			if (key.equals(IMAGE_KEY)) {// �ָ�����һ�Ŵ�ͼ
				for (String imageRecord : imageRecords) {
					/* ��ȡͼƬURL�ͱ���·�� */
					int urlstart = IMAGE_TAG.length() + 1;
					int urlend = imageRecord.indexOf("]", urlstart);
					String bigImageURL = imageRecord.substring(urlstart, urlend);
					int savefilestart = imageRecord.indexOf("[", urlend);
					int savefileend = imageRecord.lastIndexOf("]");
					String savefile = imageRecord.substring(savefilestart + 1, savefileend);
					downLoadImageFromBigImageURL(bigImageURL, savefile);
				}
			} else if (key.equals(BIG_IMAGE_URL_KEY)) {// �ָ���Сͼҳ�����ӵ���ͼ����
				for (String imageRecord : imageRecords) {
					int urlstart = BIG_IMAGE_URL_TAG.length() + 1;
					int urlend = imageRecord.indexOf("]", urlstart);
					String smallImageURL = imageRecord.substring(urlstart, urlend);
					int savefilestart = imageRecord.indexOf("[", urlend);
					int savefileend = imageRecord.lastIndexOf("]");
					String savefile = imageRecord.substring(savefilestart + 1, savefileend);
					downLoadImageFromSmallImageURL(smallImageURL, savefile);
				}
			} else if (key.equals(PHOTOSET_KEY)) {// �ָ�����һ��ͼƬ��
				for (String imageRecord : imageRecords) {
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
	 * ���δ��������
	 */
	public void continueUndone() {

		LOADER.continueUndone();
	}

	/**
	 * ����ҳ��������ͼƬ�����浽ָ��·����
	 * 
	 * @pageURL ҳ��·��
	 * 
	 * @param savePath
	 *          ͼƬ����·����
	 */
	public void downLoadPhotosetFromURL(String pageURL, String savePath) {

		LOADER.downLoadPhotosetFromURL(pageURL, savePath);
	}

	/**
	 * �������ָ����ش�ͼ��ָ��Ŀ¼��
	 * 
	 * @param bigImageURL
	 *          ������ͼƬ��ַ��
	 * 
	 * @param savefile
	 *          ����·����
	 */
	public void downLoadImageFromBigImageURL(String bigImageURL, String savefile) {

		LOADER.downLoadImageFromBigImageURL(bigImageURL, savefile);
	}

	/**
	 * �������ָ���Сͼҳ�����ӵ���ͼҳ��������ء�
	 * 
	 * @param smallImageURL
	 *          Сͼҳ�档
	 * 
	 * @param savefile
	 *          ͼƬ����·����
	 */
	public void downLoadImageFromSmallImageURL(String smallImageURL, String savefile) {

		LOADER.downLoadImageFromSmallImageURL(smallImageURL, savefile);
	}

	/**
	 * ����־�ж�ȡ�С���־��ÿһ�б���һ�����ָ���¼��
	 * 
	 * @param logger
	 *          ��־�ļ���
	 * @return ���ָ���¼��
	 */
	public Map<String, List<String>> readLineFromLogFile(File logger) {

		String line = "";
		BufferedReader reader = null;
		List<String> IMAGE = new ArrayList<String>();
		List<String> BIG_IMAGE_URL = new ArrayList<String>();
		List<String> PHOTOSET = new ArrayList<String>();
		Map<String, List<String>> recoverRecords = new HashMap<String, List<String>>();
		try {
			reader = new BufferedReader(new FileReader(logger));
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(IMAGE_TAG)) {
					IMAGE.add(line);
				} else if (line.startsWith(BIG_IMAGE_URL_TAG)) {
					BIG_IMAGE_URL.add(line);
				} else if (line.startsWith(PHOTOSET_TAG)) {
					PHOTOSET.add(line);
				}
			}
		} catch (Exception e) {
			System.err.println("\n Reading the logger occur error!");
			e.printStackTrace();
		}
		recoverRecords.put(IMAGE_KEY, IMAGE);
		recoverRecords.put(PHOTOSET_KEY, PHOTOSET);
		recoverRecords.put(BIG_IMAGE_URL_KEY, BIG_IMAGE_URL);
		return recoverRecords;// ���ָ���¼��key�����ָ�������ʶ value�����ָ������б�
	}
}
