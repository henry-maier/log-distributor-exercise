package com.distributor.distributor.config;

import com.distributor.distributor.core.LogAnalyzerServiceGetter;
import distributor.loganalyzer.grpc.LogAnalyzerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogDistributorConfiguration {
  private final Logger logger = LoggerFactory.getLogger(LogDistributorConfiguration.class);

  @Value("${analyzerAddresses}")
  private List<String> backendServerAddresses;

  @Bean
  public List<LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub> logAnalyzerServiceStubs() {
    if (backendServerAddresses.size() != backendServerWeights.size()) {
      throw new IllegalArgumentException(
          "Invalid configuration, weights and addresses length mismatch");
    }
    logger.info(" backendServerAddresses: {}", backendServerAddresses);
    logger.info("backendServerWeights: {}", backendServerWeights);
    List<LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub> logAnalyzerServiceStubs =
        new ArrayList<>();
    for (String backendServerAddress : backendServerAddresses) {
      String[] parts = backendServerAddress.split(":");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Invalid server address: " + Arrays.toString(parts));
      }
      String host = parts[0];
      int port = Integer.parseInt(parts[1]);
      // todo dont use plaintext
      ManagedChannel managedChannel =
          ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
      LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub logAnalyzerServiceStub =
          LogAnalyzerServiceGrpc.newFutureStub(managedChannel);
      logAnalyzerServiceStubs.add(logAnalyzerServiceStub);
    }
    return logAnalyzerServiceStubs;
  }

  @Value("${analyzerWeights}")
  private List<Float> backendServerWeights;

  @Value("${randomSeed}")
  private long randomSeed;

  @Bean
  public LogAnalyzerServiceGetter logAnalyzerServiceGetter() {
    return new LogAnalyzerServiceGetter(
        logAnalyzerServiceStubs(), backendServerWeights, new Random(randomSeed));
  }
}
