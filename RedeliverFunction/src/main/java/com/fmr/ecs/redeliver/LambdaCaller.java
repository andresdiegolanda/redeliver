package com.fmr.ecs.redeliver;

import java.nio.charset.StandardCharsets;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

public class LambdaCaller {

	public String handleRequest() {

		// Create an AWS Lambda client
		AWSLambda lambdaClient = AWSLambdaClientBuilder.defaultClient();

		// Set the ARN of the target Lambda function
		String targetFunctionArn = "arn:aws:lambda:eu-west-1:974396178048:function:containerjwtgenerator";

		// Create an InvokeRequest object
		InvokeRequest invokeRequest = new InvokeRequest().withFunctionName(targetFunctionArn)
				.withPayload("{\"key\": \"value\"}");

		// Invoke the target Lambda function and get the result
		InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);

		// Convert the result payload to a String and return it
		String resultPayload = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
		return resultPayload;
	}
}
