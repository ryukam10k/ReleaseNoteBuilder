package application;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;



import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FormController implements Initializable {
	
	@FXML
	private TextField verMajor;
	@FXML
	private TextField verMinor;
	@FXML
	private TextField verBuildNo;
	@FXML
	private TextField releaseDate;
	@FXML
	private TextArea changeList;
	
	/**
	 * 初期化
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		String[] version = getVersion(Main.releaseNotePath);
		verMajor.setText(version[0]);
		verMinor.setText(version[1]);
		verBuildNo.setText(version[2]);
		releaseDate.setText(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
	}
	
	/**
	 * ボタンクリック
	 * @param evt イベント
	 */
	@FXML
	protected void onButtonClick(ActionEvent evt) {
		
		String majorNo = verMajor.getText();
		String minorNo = verMinor.getText();
		String buildNo = verBuildNo.getText();
		String release = releaseDate.getText();
		String changes = changeList.getText();
		
		String revisionsDirPath = Main.releaseNotePath + "\\revisions";
		
		// revisionsフォルダを作る（無ければ）
		String buildNoDirPath = createRevisonsFolder(revisionsDirPath, majorNo, minorNo);
		
		// BUILD_NO.html（テンプレート）取得
		FileInputStream template = null;
		try {
			template = new FileInputStream(revisionsDirPath + "\\BUILD_NO.html");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// BUILD_NO.htmlを作る
		createBuildNoHtml(template, buildNoDirPath, buildNo, changes);
		
		// index.htmlを更新する
		updateIndexHtml(Main.releaseNotePath, release, majorNo, minorNo, buildNo);
		
		// ver.txt更新
		updateVerFile(Main.releaseNotePath, majorNo, minorNo, buildNo);
		
		Desktop desktop = Desktop.getDesktop();
		String uriString = "http://webapsvr01/releaseNote/";
		try {
			URI uri = new URI(uriString);
			desktop.browse(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	
	/**
	 * バージョン取得
	 * @param filePath パス
	 * @return バージョン
	 */
	private String[] getVersion(String filePath) {
		String[] version = null;
		
		try {
			FileInputStream fi = new FileInputStream(filePath + "\\ver.txt");
			InputStreamReader is = new InputStreamReader(fi, "UTF-8");
			BufferedReader br = new BufferedReader(is);
			String line = br.readLine();
			version = line.split("\\.");
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return version;
	}
	
	/**
	 * revisions配下のバージョンフォルダ作成
	 * @param revisionsDirPath パス
	 * @param majorNo メジャーVer
	 * @param minorNo マイナーVer
	 * @return 作成後のフォルダパス
	 */
	private String createRevisonsFolder(String revisionsDirPath, String majorNo, String minorNo) {
		String path = revisionsDirPath + "\\" + majorNo + "." + minorNo;
		File newfile = new File(path);
		if (!newfile.exists()) {
			newfile.mkdir();
		}
		return path;
	}
	
	/**
	 * BUILD_NO.html作成
	 * @param template テンプレート
	 * @param revisionsDirPath パス
	 * @param buildNo ビルド番号
	 * @param changes 変更内容
	 */
	private void createBuildNoHtml(FileInputStream template, String revisionsDirPath, String buildNo, String changes) {
		FileOutputStream newFile;
		String filePath = revisionsDirPath + "\\" + buildNo + ".html";
		if (new File(filePath).exists()) {
			Alert alert = new Alert(null, filePath + "は既に存在します。上書きしますか？", ButtonType.CANCEL, ButtonType.OK);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get().equals(ButtonType.CANCEL)) {
				return;
			}
		}
		
		try {
			InputStreamReader is = new InputStreamReader(template, "UTF-8");
			BufferedReader br = new BufferedReader(is);
			String line;
			
			String[] changeList = changes.split("\n");
			StringBuilder sb = new StringBuilder();
			
			for (String change : changeList) {
				sb.append("<li>");
				sb.append(change);
				sb.append("</li>");
			}
			
			//String kaigyo = System.getProperty("line.separator");
			
			newFile = new FileOutputStream(filePath);
			PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(newFile, "UTF-8")));

			while ((line = br.readLine()) != null) {
				line = line.replace("BUILD_NO", buildNo);
				line = line.replace("<li></li>", sb.toString());

				writer.println(line);
			}

			writer.close();
			newFile.close();
			template.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * index.html更新
	 * @param path パス
	 * @param release リリース日
	 * @param majorNo メジャーVer
	 * @param minorNo マイナーVer
	 * @param buildNo ビルド番号
	 */
	private void updateIndexHtml(String path, String release, String majorNo, String minorNo, String buildNo) {
		FileOutputStream newFile;
		String filePath = path + "\\index.html";
		
		try {
			FileInputStream oldFile = new FileInputStream(filePath);
			InputStreamReader is = new InputStreamReader(oldFile, "UTF-8");
			BufferedReader br = new BufferedReader(is);
			String line;
			
			String tab = "	";
			String kaigyo = System.getProperty("line.separator");
			
			StringBuilder sb = new StringBuilder();
			sb.append("<tr>");
			sb.append("<td>" + release + "</td>");
			sb.append("<td><a href='revisions/" + majorNo + "." + minorNo + "/" + buildNo + ".html'>Ver." + majorNo + "." + minorNo + "." + buildNo + "</a></td>");
			sb.append("</tr>");
			
			StringBuilder sb2 = new StringBuilder();
			
			while ((line = br.readLine()) != null) {
				line = line.replace("BUILD_NO", buildNo);
				line = line.replace("<tbody>", "<tbody>" + kaigyo + tab + tab + tab + sb.toString());
				line = line + kaigyo;
				
				sb2.append(line);
			}
			newFile = new FileOutputStream(filePath);
			PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(newFile, "UTF-8")));
			writer.print(sb2.toString());

			writer.close();
			newFile.close();
			oldFile.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateVerFile(String path, String majorNo, String minorNo, String buildNo) {
		FileOutputStream newFile;
		String filePath = path + "\\ver.txt";
		
		try {
			newFile = new FileOutputStream(filePath);
			newFile.write(new String(majorNo + "." + minorNo + "." + buildNo).getBytes());
			newFile.flush();
			newFile.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
