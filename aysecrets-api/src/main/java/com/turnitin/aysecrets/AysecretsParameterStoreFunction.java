package com.turnitin.aysecrets;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

@Slf4j
public class AysecretsParameterStoreFunction extends AysecretsGet {
	@Override
	protected String handleMethod() throws Exception {
		long start = System.currentTimeMillis();
		SsmClient ssmClient = SsmClient.builder()
				.region(Region.US_EAST_2)
				.build();
		start = System.currentTimeMillis();
		GetParameterRequest request = GetParameterRequest.builder().name("/ayacub/secret1").withDecryption(true).build();
		String value = ssmClient.getParameter(request).parameter().value();
		long took = System.currentTimeMillis() - start;
		log.info("FIXME PARAMETER STORE --> " + value + " (" + took +  ")");

		return   Long.toString(took);
	}

}
