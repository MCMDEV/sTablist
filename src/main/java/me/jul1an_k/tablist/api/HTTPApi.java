package me.jul1an_k.tablist.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class HTTPApi {
	
	public static int downloadFile(String url, String outputPath) throws IOException {
		URL site = new URL(url);
		URLConnection conn = site.openConnection();
		conn.addRequestProperty("User-Agent", "Chrome/52.0");
		
		InputStream is = new BufferedInputStream(conn.getInputStream());
		OutputStream os = new BufferedOutputStream(new FileOutputStream(outputPath));
		
		byte[] chunk = new byte[1024];
		int chunkSize;
		int count = -1;
		while((chunkSize = is.read(chunk)) != -1) {
			os.write(chunk, 0, chunkSize);
			count++;
		}
		os.flush(); // Necessary for Java < 6
		os.close();
		is.close();
		
		return count;
	}
	
	public static String readLine(String URL) throws IOException {
		URL site = new URL(URL);
		URLConnection conn = site.openConnection();
		conn.addRequestProperty("User-Agent", "Chrome/52.0");
		
		InputStreamReader isr = new InputStreamReader(conn.getInputStream());
		BufferedReader br = new BufferedReader(isr);
		
		return br.readLine();
	}
	
}
