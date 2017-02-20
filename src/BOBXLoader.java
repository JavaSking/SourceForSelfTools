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
 * BOBX��վͼƬ���������������ػָ����ơ�
 * 
 * ֧�ֶ��̡߳�
 * 
 * @author JavaSking
 *
 *         2016��12��7��
 */
public class BOBXLoader implements Runnable {

	private int photosetCount;// ��Ƭ����

	public void setPhotosetCount(int photosetCount) {

		this.photosetCount = photosetCount;
	}

	public int getPhotosetCount() {

		return this.photosetCount;
	}

	private BobxMetaData metaData;// Ԫ��Ϣ

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

		/* ��ȡ��ǰ�����ڵ�Ŀ¼ */
		String directory = BOBXLoader.class.getResource("/").getPath().substring(1);
		BufferedReader reader = null;
		String bobx = directory + "bobx.txt";// Ԫ��Ϣ�ļ�
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
			System.err.println("��ȡ�ļ���" + bobx + "���ڡ�" + count + "��ʧ��!");
		}
		for (BobxMetaData metaData : metaDatas) {
			new Thread(new BOBXLoader(metaData)).start();
		}
	}

	/* ��վ��·�� */
	private static final String ROOT = "http://www.bobx.com";

	/**
	 * ������־�����ڻָ����ơ�
	 */
	private StringBuffer errorLogger = new StringBuffer();

	/**
	 * δ����б������ʽ������ͼƬURL ����·��
	 */
	private Map<String, String> UNDONE = new HashMap<String, String>();

	/**
	 * ����Ŀ��ҳ��ͼƬ��ָ��Ŀ¼��
	 * 
	 * @param pageURL
	 *          Ŀ��ҳ�档
	 * @param savePath
	 *          ͼƬ����·����
	 */
	public void LoaderPicture(String pageURL, String savePath) {

		/* 1����ȡ��ʼҳ��Դ�� */
		System.out.println("\n" + "Loading the source of root page : " + pageURL + "\n");
		String pageContent = getHtmlContentByURL(pageURL);
		/* 2�����ݲ�ͬ��վͼƬ�Ű�ģʽ��������ȡ������ҳ��·��������ʵ�� */
		String[] childPageURLS = extractChildPageURLS(pageURL, pageContent);
		setPhotosetCount(childPageURLS.length);// ������Ƭ���� ���ڼ����Ƿ��������
		/* 3�����η�����ҳ������ͼƬ������ */
		if (childPageURLS != null) {
			for (String childpageURL : childPageURLS) {
				System.out.println("\n" + "Downloading from the children page : [" + childpageURL + "]\n");
				downLoadPhotosetFromURL(childpageURL, createDirectory(savePath));
			}
		}
		recover(savePath);// �����ָ�����
	}

	/**
	 * ������ȡ������ҳ��·����
	 * 
	 * @param pageURL
	 *          ��ҳ��URL��
	 * 
	 * @param pageContent
	 *          ��ҳ��Դ���롣
	 * 
	 * @return ��ҳ���б�
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
				System.out.println(result[i - 1]);// ��ӡ��ҳ��URL�б�
			}
		} catch (Exception ex) {
			System.err.println("\n Extracting the children pages of [" + pageURL + "] occur error!\n");
			errorLogger.append("\n Extracting the children pages of [" + pageURL + "] occur error!\n");
		}
		return result;
	}

	/**
	 * ��ȡСͼҳ��URL�б�
	 * 
	 * @param pageContent
	 *          Сͼ�б�ҳ��Դ��
	 * 
	 * @param smallImageURLS
	 *          Сͼҳ��URL�б�
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
			collectSmallImageURL(nextPageContent, smallImageURLS);// �ݹ��ȡ��һҳСͼҳ��URL
		}
	}

	public void downLoadPhotosetFromURL(String pageURL, String savePath) {

		String childPageContent = getHtmlContentByURL(pageURL);
		System.out.println(childPageContent);
		/* 0����ȡIDOL���� ����ƴװͼƬ����·�� */
		String IDOL = savePath.substring(savePath.lastIndexOf("/") + 1);
		System.out.println(IDOL);
		/* 1����ȡ�������Ƭ�����ڴ�����Ŀ¼������Ƭ����Ŀ¼���� */
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
			/* ���� */
			int FONTPRETAGINDEX = childPageContent.indexOf(FONTPRETAG);
			String title = "";
			if (FONTPRETAGINDEX == -1) {// ���һ����Ƭ��
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
				/* ��Ƭ�� */
				int BRONZEINDEX = childPageContent.indexOf(BRONZE);
				int numberend = childPageContent.indexOf(PIX, BRONZEINDEX + BRONZE.length() + 1);
				String P = childPageContent.substring(BRONZEINDEX + BRONZE.length(), numberend - 1);
				count = Integer.parseInt(P);
				photoset = title + " " + IDOL + " [" + P + "P]";// ��װ��Ƭ�����ƣ����� ģ���� [����]
			}
			if (photoset.contains("<br>")) {
				photoset = photoset.replaceAll("<br>", "");// ������⻻��BUG
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
		if (subDirectory.listFiles().length == count) {/* �����Ƭ���Ѿ����������ֱ�ӷ��� */
			return;
		}
		/* 2���Ѽ�Сͼ����ҳ�棬�������ӵ���ͼҳ��������� */
		List<String> smallImageURLS = new ArrayList<String>();
		try {
			collectSmallImageURL(childPageContent, smallImageURLS);
		} catch (Exception e) {
			System.err.println("\n" + "Collecting ths small image page urls from [" + pageURL + "] occur error!\n");
			errorLogger.append("\n" + "Collecting ths small image page urls from [" + pageURL + "] occur error!\n");
		}
		if (smallImageURLS.isEmpty())
			return;
		int index = 1;// ��Ƭ����
		for (String smallImageURL : smallImageURLS) {
			String filename = photoset + "(" + index++ + ")" + ".jpg";// �����ļ���
			String savefile = savePath + File.separator + photoset + File.separator + filename;// ����·��
			downLoadImageFromSmallImageURL(smallImageURL, savefile);
		}
	}

	/**
	 * ��Сͼҳ�����ӵ���ͼҳ��������ء�
	 * 
	 * @param smallImageURL
	 *          Сͼҳ�档
	 * 
	 * @param savefile
	 *          ͼƬ����·����
	 */
	public void downLoadImageFromSmallImageURL(String smallImageURL, String savefile) {

		if (new File(savefile).exists()) {
			System.out.println("\nThe big image of small image : [" + smallImageURL + "] is already loaded to local [" + savefile + "]!\n");
			return;/* �ļ��Ѿ����ڣ�ֱ�ӷ��� */
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
	 * ���ش�ͼ��ָ��Ŀ¼��
	 * 
	 * @param bigImageURL
	 *          ��ͼ·����
	 * 
	 * @param savefile
	 *          ָ������Ŀ¼��
	 */
	public void downLoadImageFromBigImageURL(String bigImageURL, String savefile) {

		if (new File(savefile).exists()) {
			System.out.println("\nThe big image : [" + bigImageURL + "] is already loaded to local [" + savefile + "]!\n");
			return;/* �ļ��Ѿ����ڣ�ֱ�ӷ��� */
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
			UNDONE.put(bigImageURL, savefile);// ����δ����б�
		}
	}

	public String createDirectory(String savePath) {

		return savePath;
	}

	/**
	 * ����δ�������
	 */
	public void continueUndone() {

		System.out.println("\n Loading the undone image!\n");
		/* 2���������δ���ص��ļ� */
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
	 * �ָ����ƣ���¼��־������жϵ���������
	 */
	public void recover(String savePath) {

		/* 1��д��־�ļ� */
		String logfileName = savePath.substring(savePath.lastIndexOf(File.separator) + 1);
		try {
			File logFile = new File(savePath + File.separator + logfileName + "��־.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
			writer.write(errorLogger.toString());
			if (writer != null) {
				writer.close();
			}
		} catch (Exception e) {
			System.err.println("\n Error Write the log file!\n");
		}
		continueUndone();// ���δ��������
		errorLogger = new StringBuffer();// �����־
		UNDONE.clear();// ���δ����б�
		checkTarget(savePath);
	}

	/**
	 * �������������Ƿ���ɡ�
	 */
	public void checkTarget(String savePath) {

		String idol = savePath.substring(savePath.lastIndexOf(File.separator) + 1);
		boolean result = true;
		StringBuffer buffer = new StringBuffer();
		/* 1�������Ƭ���� */
		File directory = new File(savePath);
		File[] subFiles = directory.listFiles();
		if (subFiles.length != getPhotosetCount()) {
			result = false;
		}
		/* 2����������Ƭ����Ƭ�� */
		for (File subFile : subFiles) {
			String subFileName = subFile.getName();
			buffer.append(subFileName).append("\n");
			int start = subFileName.lastIndexOf("[");
			int end = subFileName.lastIndexOf("P]");
			int count = Integer.parseInt(subFileName.substring(start + 1, end));// ��Ƭ����Ƭ��
			if (count != subFile.listFiles().length) {
				result = false;
			}
		}
		if (result) {
			try {
				// д��־�ļ�
				new File(savePath + File.separator + idol + "_true.txt").createNewFile();
				// д�鵵�ļ�
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

	String NAME;// ͼƬĿ¼����

	String STARTURL;// ��ʼURL

	int LENGTH;// �������

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
