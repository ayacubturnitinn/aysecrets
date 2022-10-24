package com.turnitin.aysecrets;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;

@Slf4j
public class AysecretsCacheFunction  extends AysecretsGet {
	private final SecretCache cache = new SecretCache(AWSSecretsManagerClientBuilder.standard().withRegion(Region.US_EAST_2.toString()).build());

	@Override
	protected String handleMethod() throws Exception {
		long start = System.currentTimeMillis();
		final String value = cache.getSecretString("ayacub-test-secret");
		long took = System.currentTimeMillis() - start;
		log.info("FIXME CACHE  --> " + value + " (" + took +  ")");

		return  Long.toString(took);
	}

}
