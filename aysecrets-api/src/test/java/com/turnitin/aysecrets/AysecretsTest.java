package com.turnitin.aysecrets;

import com.amazonaws.AbortedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.UpdateFunctionConfigurationRequest;


class AysecretsTest {
	enum RUN_TYPE {
		COLD, WARM
	}

	static Map<String, Invocations> invocationsMap = new HashMap<>();
	static final int COLD_STARTS = 5;
	static final int WARMS_PER_COLD = 10;


//	public static void main(String[] args) throws Exception {
//		URL url = new URL("https://jaua7aw5ha.execute-api.us-east-2.amazonaws.com/Prod/secret/aysecrets-AysecretsHelloWorld2Function-NMWXFcrnc7c9");
//		HttpURLConnection con = (HttpURLConnection) url.openConnection();
//		con.setRequestMethod("GET");
//		InputStream responseStream = con.getInputStream();
//		System.out.println(new ObjectMapper().readValue(responseStream, Map.class));
//	}
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();

//		getLogs("hello");
//		getLogs("hello2");
//		getLogs("sm");
//		getLogs("ps");
//		getLogs("ks");
//		getLogs("cache");


//		testMethod("hello", "aysecrets-AysecretsHelloWorldFunction-cQoCAt9YT3QR");
		testMethod("hello2", "aysecrets-AysecretsHelloWorld2Function-NZePzarIocX6");
		testMethod("sm", "aysecrets-AysecretsSecretManagerFunction-8p9ssbkgZ7KL");
		testMethod("ps", "aysecrets-AysecretsParameterStoreFunction-xQDT6ukg061l");
		testMethod("ks", "aysecrets-AysecretsKeystoreFunction-zwtfz2d9PdcW");
		testMethod("cache", "aysecrets-AysecretsCacheFunction-EDjqsXOefC58");
		testMethod("ext", "aysecrets-AysecretsExtensionFunction-MJrISpXBDKlQ");

		List<String> keys = new ArrayList<>(invocationsMap.keySet());
		Collections.sort(keys);
		for (String k: keys) {
			Invocations invocations = invocationsMap.get(k);
//			System.out.println(k + "\t\t " + invocations.num + "\t" + invocations.avg() + "\t" + invocations.totalAvg());
			System.out.println(k + "\t\t " + invocations.num + "\t" + invocations.avg() );
		}

		System.out.println("\n\n\nTook: " + (System.currentTimeMillis() - start));
	}

	private final static Pattern COLD_PATTERN =  Pattern.compile("^.*Duration: (\\d+).\\d+ ms\t.*Init Duration: (\\d+).\\d+ ms.*");
	private final static Pattern WARM_PATTERN =  Pattern.compile("^.*Duration: (\\d+).\\d+ ms\t.*");

	private static void getLogs(String file) throws IOException {
		InputStream is = AysecretsTest.class.getClassLoader().getResourceAsStream(file + ".txt");
		try (InputStreamReader streamReader =
				new InputStreamReader(is, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(streamReader)) {

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("Init Duration")) {
					Matcher m = COLD_PATTERN.matcher(line);
					if (!m.find()) {
						throw new RuntimeException("NO COLD ==>" + line);
					}
					long duration = Long.parseLong(m.group(1));
					long initDuration = Long.parseLong(m.group(2));
					addInvocation(file, RUN_TYPE.COLD, duration  + initDuration, 0L);
				}
				else if (line.contains("Duration")) {
					Matcher m = WARM_PATTERN.matcher(line);
					if (!m.find()) {
						throw new RuntimeException("NO COLD ==>" + line);
					}
					long duration = Long.parseLong(m.group(1));
					addInvocation(file, RUN_TYPE.WARM, duration, 0L);
				}
				else {
					System.out.println("NON DURATION LINE: " + line);
				}
			}

		}

		System.out.printf("|%s  |  %.2f  | %.2f |\n", file, invocationsMap.get(file+ "_COLD").avg(),   invocationsMap.get(file+ "_WARM").avg());
	}

	private static void testMethod(String lambda,String fnId) throws Exception {
		LambdaClient lambdaClient = LambdaClient.builder().region(Region.US_EAST_2).build();
		URL url = new URL("https://jaua7aw5ha.execute-api.us-east-2.amazonaws.com/Prod/secret/" + lambda);

		for (int cold = 0; cold < COLD_STARTS; cold++) {
			System.out.println("updating config " + lambda + "  " + cold);
			lambdaClient.updateFunctionConfiguration(UpdateFunctionConfigurationRequest.builder()
															 .functionName(fnId)
															 .timeout(20 + cold)
															 .build());
			Thread.sleep(2000);

			long start = System.currentTimeMillis();
			Long took = runLambda(url);
			long totalTook = System.currentTimeMillis() - start;
			System.out.println("took = " + took);
			addInvocation(lambda, RUN_TYPE.COLD, took, totalTook);

			for (int warm = 0; warm < WARMS_PER_COLD; warm++) {
				start = System.currentTimeMillis();
				took = runLambda(url);
				totalTook = System.currentTimeMillis() - start;
				System.out.println("took = " + took);
				addInvocation(lambda, RUN_TYPE.WARM, took, totalTook);
			}
		}
	}

	private static void addInvocation(String lambda, RUN_TYPE runType, Long took, Long tookTotal) {
		String key = lambda + "_" + runType;
		invocationsMap.putIfAbsent(key, new Invocations());
		invocationsMap.get(key).num++;
		invocationsMap.get(key).durations.add(took);
		invocationsMap.get(key).totalDurations.add(tookTotal);
	}

	private static long runLambda(URL url) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		InputStream responseStream = con.getInputStream();
		return new ObjectMapper().readValue(responseStream, Long.class);
	}

	private static class Invocations {
		public int num = 0;
		public List<Long> durations = new ArrayList<>();
		public List<Long> totalDurations = new ArrayList<>();

		public double avg() {
			Long total = 0L;
			for (Long duration : durations) {
				total += duration;
			}
			return ((double)total /(double)num);
		}

		public double totalAvg() {
			Long total = 0L;
			for (Long duration : totalDurations) {
				total += duration;
			}
			return ((double)total /(double)num);
		}
	}

}

