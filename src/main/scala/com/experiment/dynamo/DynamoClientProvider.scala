package com.experiment.dynamo

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsync, AmazonDynamoDBAsyncClientBuilder}

class DynamoClientProvider(endpoint: String, accessKey: String, secretKey: String) {

  lazy val client: AmazonDynamoDBAsync =
    AmazonDynamoDBAsyncClientBuilder
      .standard()
      .withEndpointConfiguration(new EndpointConfiguration(endpoint, Regions.US_EAST_1.getName))
      .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
      .build()

}
