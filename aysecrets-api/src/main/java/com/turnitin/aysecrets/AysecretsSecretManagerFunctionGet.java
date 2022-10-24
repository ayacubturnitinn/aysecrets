package com.turnitin.aysecrets;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@Slf4j
public class AysecretsSecretManagerFunctionGet extends AysecretsGet {
	@Override
	protected String handleMethod() throws Exception {
		long start = System.currentTimeMillis();
		SecretsManagerClient secMgrClient = SecretsManagerClient.builder()
				.region(Region.US_EAST_2)
				.build();

		GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
				.secretId("ayacub-test-secret")
				.build();
		String value = secMgrClient.getSecretValue(getSecretValueRequest).secretString();
		long took = System.currentTimeMillis() - start;

		log.info("FIXME SECRET MANAGER --> " + value + " (" + took +  ")");

		return Long.toString(took);
	}

}
