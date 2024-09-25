package com.lsx.bigtalk.api;

import androidx.annotation.NonNull;

import com.lsx.bigtalk.logs.Logger;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpClient {
	private static final Logger logger = Logger.getLogger(HttpClient.class);

	// multipart/form-data example as follow:
	/*
		POST / HTTP/1.1
		HOST: host.example.com
		Cookie: some_cookies...
		Connection: Keep-Alive
		Content-Type: multipart/form-data;boundary=---------7d4a6d158c9

		-----------7d4a6d158c9
		Content-Disposition: form-data;name="file1";filename="fileName1"
		Content-Type: application/octet-stream

		file content1
		-----------7d4a6d158c9
		Content-Disposition: form-data;name="file2";filename="fileName2"
		Content-Type: application/octet-stream

		file content2
		-----------7d4a6d158c9
		Content-Disposition: form-data;name="file3";filename="fileName3"
		Content-Type: application/octet-stream

		file content3
		-----------7d4a6d158c9--
	 */
	public String uploadImage(String imageUrl, byte[] bytes, String fileName) {
		logger.d("HttpClient#uploadImage url: %s, fileName: %s", imageUrl, fileName);
		try {
			String BOUNDARY = "---------7d4a6d158c9";
			HttpURLConnection httpURLConnection = getHttpURLConnection(imageUrl, BOUNDARY);
			OutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
			File file = new File(fileName);

			String dataString = "--" + BOUNDARY + "\r\n" +
					"Content-Disposition:form-data;name=\"file0\";filename=\"" + file.getName() + "\"\r\n" +
					"Content-Type:application/octet-stream\r\n\r\n";
			outputStream.write(dataString.getBytes());
			outputStream.write(bytes);
			outputStream.write("\r\n".getBytes());
			outputStream.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());
			outputStream.flush();
			outputStream.close();

			BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			String line = reader.readLine();
			if (null != line) {
				logger.d("remote image: %s", line);
				/*
				 * {
				 *     "error_code":0,
				 *     "error_msg": "成功",
				 *     "path": "g0/000/000/1410706133246550_140184328214.jpg",
				 *     "url": "http://122.225.68.125:8001/g0/000/000/1410706133246550_140184328214.jpg"
				 * }
				 */
				JSONObject root = new JSONObject(line);
				return root.getString("url");
			}
			return "";
		} catch (Exception e) {
			logger.e("uploadImage error: %s", e);
		}
		return "";
	}

	private static @NonNull HttpURLConnection getHttpURLConnection(String imageUrl, String BOUNDARY) throws IOException {
		URL url = new URL(imageUrl);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setDoInput(true);
		httpURLConnection.setUseCaches(false);
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
		httpURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
		httpURLConnection.setRequestProperty("Charset", "UTF-8");
		httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
		return httpURLConnection;
	}
}
