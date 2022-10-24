package com.turnitin.aysecrets;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.turnitin.aysecrets.service.AysecretsService;
import com.turnitin.commons.TurnitinContext;
import com.turnitin.commons.http.HttpMethod;
import com.turnitin.commons.lambda.ApiGatewayLambda;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

@Slf4j
public class AysecretsGet extends ApiGatewayLambda<String> {

	private AysecretsService aysecretsservice;

	// This constructor is for regular flow
	public AysecretsGet() {
		this.ctx = TurnitinContext.builder()
				.addEnvironmentVariable("KEY_ID")
				.addEnvironmentVariable("KMS_SECRET")
				.build();
		this.aysecretsservice = new AysecretsService(ctx);
	}

	// This Constructor is use in tests if you want to mock the context or parts there of.
	public AysecretsGet(TurnitinContext ctx) {
		this.ctx = ctx;
		this.aysecretsservice = new AysecretsService(ctx);
	}

	private final SecretCache cache = new SecretCache(AWSSecretsManagerClientBuilder.standard().withRegion(Region.US_EAST_2.toString()).build());

	@Override
	protected String handleMethod() throws Exception {
		long start = System.currentTimeMillis();

		final String fromCache = cache.getSecretString("ayacub-test-secret");
//		final String fromCache = "FAKE";
		long cacheTime = System.currentTimeMillis() - start;

		SsmClient ssmClient = SsmClient.builder()
				.region(Region.US_EAST_2)
				.build();
		start = System.currentTimeMillis();
		GetParameterRequest request = GetParameterRequest.builder().name("/ayacub/secret1").withDecryption(true).build();
		String value = ssmClient.getParameter(request).parameter().value();
		long parameterStoreTime = System.currentTimeMillis() - start;
		log.info("FIXME AMTBackendAPIGetSecret parameter store --> " + value);


		SecretsManagerClient secMgrClient = SecretsManagerClient.builder()
				.region(Region.US_EAST_2)
				.build();


		start = System.currentTimeMillis();
		GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
				.secretId("ayacub-test-secret")
				.build();
		String secretMgrValue = secMgrClient.getSecretValue(getSecretValueRequest).secretString();
		long secretMgrTime = System.currentTimeMillis() - start;

		log.info("FIXME AMTBackendAPIGetSecret secret manager --> " + secretMgrValue);


		return value + " (parameterStoreTime = " + parameterStoreTime +  ") "
				+ " <-------->" + secretMgrValue + " (secretMgrTime: " + secretMgrTime +  ")"
				+ " <-------->" + fromCache + " (cacheTime: " + cacheTime +  ")";
	}

	@Override
	protected List<String> getSupportedHttpMethods() {
		return Arrays.asList(HttpMethod.GET.name());
	}
}
