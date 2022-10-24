package com.turnitin.aysecrets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turnitin.aysecrets.service.AysecretsService;
import com.turnitin.commons.TurnitinContext;
import com.turnitin.commons.http.HttpMethod;
import com.turnitin.commons.lambda.ApiGatewayLambda;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.slf4j.MDC;

@Slf4j
public class AysecretsHelloWorld2Function extends ApiGatewayLambda<String> {

	private AysecretsService aysecretsservice;

	private Map<String, Long> times = new HashMap<>();

	// This constructor is for regular flow
	public AysecretsHelloWorld2Function() {
		long start = System.currentTimeMillis();
		this.ctx = TurnitinContext.builder().build();
		times.put("ctx builder", System.currentTimeMillis()  - start); start = System.currentTimeMillis();

		this.aysecretsservice = new AysecretsService(ctx);
		times.put("constructor", System.currentTimeMillis()  - start); start = System.currentTimeMillis();
	}

	// This Constructor is use in tests if you want to mock the context or parts there of.
	public AysecretsHelloWorld2Function(TurnitinContext ctx) {
		this.ctx = ctx;
		this.aysecretsservice = new AysecretsService(ctx);
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		long realStart = System.currentTimeMillis();
		long start = System.currentTimeMillis();
		this.input = input;
		ctx.setContextData(input.getHeaders());
		times.put("setContextData", System.currentTimeMillis()  - start); start = System.currentTimeMillis();
		response = ctx.createOkProxyResponseEvent();
		times.put("createOkProxyResponseEvent", System.currentTimeMillis()  - start); start = System.currentTimeMillis();
		MDC.put("ESTraceId", ctx.getTraceHeaderValueForLogs(input.getHeaders()));
		if (log.isTraceEnabled()) log.trace(ctx.serialize(input));
		times.put("ctx.serialize(input)", System.currentTimeMillis()  - start); start = System.currentTimeMillis();
		if (!getSupportedHttpMethods().contains(input.getHttpMethod())) {
			log.error("HTTP Method [{}] not supported for path [{}]", input.getHttpMethod(), input.getPath());
			response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
			return response;
		}
		try {
			log.info("input path: {}", input.getPath());
			times.put("getSupportedHttpMethods()", System.currentTimeMillis()  - start); start = System.currentTimeMillis();
			String t = handleMethod();
			times.put("handleMethod", System.currentTimeMillis()  - start); start = System.currentTimeMillis();
			String responseBody = serialize(t);
			log.debug("response body: {}", responseBody);
			response.setBody(responseBody);
			response.withIsBase64Encoded(true);
			times.put("response", System.currentTimeMillis()  - start); start = System.currentTimeMillis();
		} catch (final Exception e) {
			exceptionHandler(e);
		}
		long sum = 0L;
		for (Long t : times.values()) {
			sum += t;
		}
		times.put("sum",sum);
		response.setBody("" + (System.currentTimeMillis()  - realStart));

		return response;
	}




	@Override
	protected String handleMethod() throws Exception {
		return "0";
	}
	@Override
	protected List<String> getSupportedHttpMethods() {
		return Arrays.asList(HttpMethod.GET.name());
	}
}
