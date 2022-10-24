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
public class AysecretsTestFunctionGet extends ApiGatewayLambda<String> {

	private AysecretsService aysecretsservice;

	// This constructor is for regular flow
	public AysecretsTestFunctionGet() {
		this.ctx = TurnitinContext.builder()
				.addEnvironmentVariable("KEY_ID")
				.addEnvironmentVariable("KMS_SECRET")
				.build();
		this.aysecretsservice = new AysecretsService(ctx);
	}

	// This Constructor is use in tests if you want to mock the context or parts there of.
	public AysecretsTestFunctionGet(TurnitinContext ctx) {
		this.ctx = ctx;
		this.aysecretsservice = new AysecretsService(ctx);
	}

	private final SecretCache cache = new SecretCache(AWSSecretsManagerClientBuilder.standard().withRegion(Region.US_EAST_2.toString()).build());

	@Override
	protected String handleMethod() throws Exception {
		long start = System.currentTimeMillis();


		return System.getenv("AWS_LAMBDA_RUNTIME_API");
	}

	@Override
	protected List<String> getSupportedHttpMethods() {
		return Arrays.asList(HttpMethod.GET.name());
	}
}
