package org.ox17.kcimagecollector;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class Helpers {
	
	private static List<MessageCallback> msgCallbacks = new LinkedList<MessageCallback>();
	
	public static void log(String msg) {
		for(MessageCallback callback : msgCallbacks) {
			callback.action(msg);
		}
		System.out.println(msg);
		System.out.flush();
	}

	public static String loadUrlIntoStr(String urlStr) throws Exception {
		URL url = new URL(urlStr);
		URLConnection con = url.openConnection();
		
		String charset;
		if(con.getContentType() == null) {
			charset = "ISO-8859-1";
		}
		else {
			Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
			Matcher m = p.matcher(con.getContentType());
			/* If Content-Type doesn't match this pre-conception, choose default and 
			 * hope for the best. */
			charset = m.matches() ? m.group(1) : "ISO-8859-1";
		}
		
		Reader r = new InputStreamReader(con.getInputStream(), charset);
		StringBuilder buf = new StringBuilder();
		
		try {
		while (true) {
		  int ch = r.read();
		  if (ch < 0)
		    break;
		  buf.append((char) ch);
		}
		} catch(Exception e) { return ""; }
		return buf.toString();
	}
	
	public static void writeStrToFile(String str, String outFilename) throws IOException {
		FileWriter fw = new FileWriter(outFilename);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(str);
		bw.close();
		fw.close();
	}
	
	public static Image imgFromUrl(String urlStr) throws Exception {
		return Toolkit.getDefaultToolkit().getImage(new URL(urlStr));
	}

	public static void addLogCallback(MessageCallback msgCallback) {
		msgCallbacks.add(msgCallback);
	}

	public static String getImgFilenameFromUrl(String imgUrl) {
		String[] parts = imgUrl.split("/");
		return parts[parts.length-1];
	}

	public static void loadUrlIntoFile(String imgUrl, File f) throws Exception {
		URL url = new URL(imgUrl);
		BufferedImage bi = ImageIO.read(url);
		ImageIO.write(bi, getExtensionFromImgName(f.getName()), f);
	}

	private static String getExtensionFromImgName(String imgName) {
		String[] parts = imgName.split("\\.");
		return parts[parts.length-1];
	}
	
}
