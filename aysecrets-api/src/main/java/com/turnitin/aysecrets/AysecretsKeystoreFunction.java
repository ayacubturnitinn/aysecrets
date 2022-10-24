package com.turnitin.aysecrets;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;

@Slf4j
public class AysecretsKeystoreFunction  extends AysecretsGet {
	@Override
	protected String handleMethod() throws Exception {
		long start = System.currentTimeMillis();

		String keyId = ctx.getVariable("KEY_ID");
		String kmsSecret = ctx.getVariable("KMS_SECRET");

		System.out.println("keyId = " + keyId);
		System.out.println("kmsSecret = " + kmsSecret);
		KmsClient kmsClient = KmsClient.builder().region(Region.US_EAST_2).build();


		DecryptRequest decryptRequest = DecryptRequest.builder()
				.ciphertextBlob(SdkBytes.fromByteArray(Base64.getDecoder().decode(kmsSecret)))
				.keyId(keyId)
				.build();

		long start2 = System.currentTimeMillis();
		DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
		long took2 = System.currentTimeMillis() - start2;

		String value = decryptResponse.plaintext().asUtf8String();

		long took = System.currentTimeMillis() - start;

		log.info("FIXME KMS  --> " + value + " (" + took +  " ==== " + took2+ ")");

		return  Long.toString(took);
	}


//
//
//		SdkBytes myBytes = SdkBytes.fromString("ABC123", StandardCharsets.UTF_8);
//		EncryptRequest encryptRequest = EncryptRequest.builder()
//				.keyId(keyId)
//				.plaintext(myBytes)
//				.build();
//
//		EncryptResponse response = kmsClient.encrypt(encryptRequest);
//		SdkBytes encryptedData = response.ciphertextBlob();
//		System.out.println("encryptedData.asString() = " + Base64.getEncoder().encodeToString(encryptedData.asByteArrayUnsafe()));
//
//		System.out.println("=====================================================");




}
