import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ExtractDownLoadURL {

	public void extractDownLoadURL() {

		String directory = this.getClass().getResource("/").getPath().substring(1);
		File[] subFiles = new File(directory).listFiles();
		for (File subFile : subFiles) {
			if (subFile.isFile() && subFile.getName().contains("��̳����")) {
				String saveFile = directory + subFile.getName().replaceAll("��̳����", "��������");
				BufferedReader reader = null;
				BufferedWriter writer = null;
				StringBuffer buffer = new StringBuffer();
				try {
					reader = new BufferedReader(new FileReader(subFile));
					String line;
					while ((line = reader.readLine()) != null) {
						String url = line.substring(5, line.indexOf("]"));
						String title = line.substring(line.indexOf("]") + 1, line.lastIndexOf("[") - 4);
						buffer.append(title).append("\n");
						buffer.append("  " + url).append("\n");
						buffer.append("\n");
					}
					writer = new BufferedWriter(new FileWriter(new File(saveFile)));
					writer.write(buffer.toString());
					writer.close();
				} catch (Exception ex) {
					System.err.println("��ȡ��̳�ļ���" + subFile.getAbsolutePath() + "������������ʧ�ܣ�");
				}
			}
		}
	}

	public static void main(String[] args) {

		new ExtractDownLoadURL().extractDownLoadURL();
	}
}
