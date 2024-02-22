package com.distributor.distributor.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import distributor.loganalyzer.grpc.LogAnalyzerServiceGrpc;
import distributor.loganalyzer.grpc.LogAnalyzerServiceOuterClass;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LogAnalyzerServiceGetterTest {

  @Test
  void testGetLogAnalyzerServiceStub_NoPosition() {
    // Stub setup
    List<LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub> stubs = new ArrayList<>();
    // Mocked stubs
    LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub stub1 =
        Mockito.mock(LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub.class);
    LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub stub2 =
        Mockito.mock(LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub.class);
    stubs.add(stub1);
    stubs.add(stub2);

    // Weights setup
    List<Float> weights = new ArrayList<>();
    weights.add(0.6f); // Weight for stub1
    weights.add(0.4f); // Weight for stub2

    // Initialize the LogAnalyzerServiceGetter
    LogAnalyzerServiceGetter serviceGetter =
        new LogAnalyzerServiceGetter(stubs, weights, new Random(42));

    // Counters for the number of times each stub is returned
    int countStub1 = 0;
    int countStub2 = 0;
    int countPos1 = 0;
    int countPos2 = 0;
    int totalIterations = 100000; // Large number of iterations for accuracy

    // Test getLogAnalyzerServiceStub method
    for (int i = 0; i < totalIterations; i++) {

      // Get the LogAnalyzerServiceFutureStub
      LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub stub =
          serviceGetter.getLogAnalyzerServiceStub();

      // Increment the counter for the returned stub
      if (stub == stub1) {
        countStub1++;
      } else if (stub == stub2) {
        countStub2++;
      }

      int pos = serviceGetter.getWeightedPosition();
      if (pos == 0) {
        countPos1++;
      } else if (pos == 1) {
        countPos2++;
      }
    }

    // Calculate the proportion of times each stub is returned
    double proportionStub1 = (double) countStub1 / totalIterations;
    double proportionStub2 = (double) countStub2 / totalIterations;
    double proportionPos1 = (double) countPos1 / totalIterations;
    double proportionPos2 = (double) countPos2 / totalIterations;

    //        System.out.println("Proportion of times stub1 is returned: " + proportionStub1);
    //        System.out.println("Proportion of times stub2 is returned: " + proportionStub2);
    // Assert that the proportions are roughly equal to the weights
    assertEquals(0.6, proportionStub1, 0.05); // Allow for a 5% margin of error
    assertEquals(0.4, proportionStub2, 0.05); // Allow for a 5% margin of error
    assertEquals(0.6, proportionPos1, 0.05); // Allow for a 5% margin of error
    assertEquals(0.4, proportionPos2, 0.05); // Allow for a 5% margin of error
  }

  @Test
  void testGetLogAnalyzerServiceStub_WithOffset() {
    // Stub setup
    List<LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub> stubs = new ArrayList<>();
    // Mocked stubs using Mockito
    LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub stub1 =
        Mockito.mock(LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub.class);
    LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub stub2 =
        Mockito.mock(LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub.class);
    LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub stub3 =
        Mockito.mock(LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub.class);
    stubs.add(stub1);
    stubs.add(stub2);
    stubs.add(stub3);

    // Weights setup
    List<Float> weights = new ArrayList<>();
    weights.add(0.4f); // Weight for stub1
    weights.add(0.3f); // Weight for stub2
    weights.add(0.3f); // Weight for stub3

    // Initialize the LogAnalyzerServiceGetter
    LogAnalyzerServiceGetter serviceGetter =
        new LogAnalyzerServiceGetter(stubs, weights, new Random(42));

    // LogPacket setup
    LogAnalyzerServiceOuterClass.LogPacket logPacket =
        LogAnalyzerServiceOuterClass.LogPacket.newBuilder().build();

    // Test getLogAnalyzerServiceStub method for different offsets
    int numStubs = stubs.size();
    int initialPos = serviceGetter.getWeightedPosition();
    LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub prevStub =
        serviceGetter.getLogAnalyzerServiceStub(initialPos);

    for (int offset = 1; offset < numStubs; offset++) {
      // Get the LogAnalyzerServiceFutureStub with the current offset
      LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub stub =
          serviceGetter.getLogAnalyzerServiceStub(offset + initialPos);
      assertNotSame(prevStub, stub);
      prevStub = stub;
    }
  }
}
