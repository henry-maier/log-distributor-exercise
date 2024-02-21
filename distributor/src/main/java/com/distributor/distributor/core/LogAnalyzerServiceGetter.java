package com.distributor.distributor.core;

import distributor.loganalyzer.grpc.LogAnalyzerServiceGrpc;
import java.util.List;
import java.util.Random;

public class LogAnalyzerServiceGetter {

  private final LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub[] stubs;
  private final float[] cumulativeWeights;
  private final Random random;

  public LogAnalyzerServiceGetter(
      List<LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub> stubs,
      List<Float> weights,
      Random r) {
    this.stubs = stubs.toArray(new LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub[0]);
    this.cumulativeWeights = new float[weights.size()];
    float sum = 0;
    for (int i = 0; i < weights.size(); i++) {
      sum += weights.get(i);
      this.cumulativeWeights[i] = sum;
    }
    this.random = r;
  }

  // should be thread safe
  public LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub getLogAnalyzerServiceStub() {
    float r = random.nextFloat();
    for (int i = 0; i < cumulativeWeights.length; i++) {
      if (r < cumulativeWeights[i]) {
        return stubs[i];
      }
    }
    // should be unreachable but just in case
    return stubs[stubs.length - 1];
  }
}
