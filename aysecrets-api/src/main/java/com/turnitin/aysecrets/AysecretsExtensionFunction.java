package com.turnitin.aysecrets;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AysecretsExtensionFunction  extends AysecretsGet {

	@Override
	protected String handleMethod() throws Exception {
		long start = System.currentTimeMillis();


		log.info("AWS_SESSION_TOKEN ==>"  + System.getenv("AWS_SESSION_TOKEN"));

		try {
			doGet();
		} catch (Exception e) {
			log.error("failed :-( ", e);
		}

		long took = System.currentTimeMillis() - start;

		return  Long.toString(took);
	}

	private void doGet() throws Exception {
		String parameterName = "/ayacub/secret1";
		parameterName = URLEncoder.encode(parameterName, StandardCharsets.UTF_8.toString());
		URL url = new URL("http://localhost:2773/systemsmanager/parameters/get?name=" + parameterName + "&withDecryption=true");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("X-Aws-Parameters-Secrets-Token", System.getenv("AWS_SESSION_TOKEN"));
		InputStream responseStream = con.getInputStream();
		log.info("doGet2 -->" + new String(responseStream.readAllBytes(), StandardCharsets.UTF_8));
	}
}
