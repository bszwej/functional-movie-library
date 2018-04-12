package com.experiment.repository.interpreter

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsync, AmazonDynamoDBAsyncClientBuilder}
import com.experiment.BaseTest

trait BaseRepositoryTest extends BaseTest {

  private val dynamoEndpoint = "http://localhost:8000"

  protected val dynamoClient: AmazonDynamoDBAsync = AmazonDynamoDBAsyncClientBuilder
    .standard()
    .withEndpointConfiguration(new EndpointConfiguration(dynamoEndpoint, Regions.US_EAST_1.getName))
    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("dev", "dev")))
    .build()

}
