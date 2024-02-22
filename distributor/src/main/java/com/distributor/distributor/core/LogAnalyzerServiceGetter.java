package com.distributor.distributor.core;

import distributor.loganalyzer.grpc.LogAnalyzerServiceGrpc;
import distributor.loganalyzer.grpc.LogAnalyzerServiceOuterClass;

import java.util.List;

import static com.distributor.distributor.core.UniqueFloatGenerator.generateUniqueFloat;

public class LogAnalyzerServiceGetter {

  private final LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub[] stubs;
  private final float[] cumulativeWeights;

  /**
   * Constructor for LogAnalyzerServiceGetter
   * @param stubs the LogAnalyzerServiceFutureStubs to use
   * @param weights the weights, in the same order as the stubs, to use to determine how often to use each stub
   */
  public LogAnalyzerServiceGetter(
      List<LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub> stubs,
      List<Float> weights) {
    this.stubs = stubs.toArray(new LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub[0]);
    this.cumulativeWeights = new float[weights.size()];
    float sum = 0;
    for (int i = 0; i < weights.size(); i++) {
      sum += weights.get(i);
      this.cumulativeWeights[i] = sum;
    }
  }

  /**
   * Get a LogAnalyzerServiceFutureStub for a given LogPacket based on this LogAnalyzerServiceGetter's weights
   * @param logPacket the LogPacket to use to determine which LogAnalyzerServiceFutureStub to return
   * @return a LogAnalyzerServiceFutureStub
   */
  public LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub getLogAnalyzerServiceStub(LogAnalyzerServiceOuterClass.LogPacket logPacket) {
    return getLogAnalyzerServiceStub(logPacket, 0);
  }

  /**
   * Get a LogAnalyzerServiceFutureStub for a given LogPacket based on this LogAnalyzerServiceGetter's weights, then get
   * a different LogAnalyzerServiceFutureStub based on the offset
   * @param logPacket the LogPacket to use to determine which LogAnalyzerServiceFutureStub to return
   * @param offset the offset to use to get a different LogAnalyzerServiceFutureStub
   * @return a LogAnalyzerServiceFutureStub
   */
  public LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub getLogAnalyzerServiceStub(LogAnalyzerServiceOuterClass.LogPacket logPacket, int offset) {
    float r = generateUniqueFloat(logPacket);
    for (int i = 0; i < cumulativeWeights.length; i++) {
      if (r < cumulativeWeights[i]) {
        return stubs[Math.floorMod(i + offset, stubs.length)];
      }
    }
    // should be unreachable but just in case
    return stubs[Math.floorMod(stubs.length - 1 + offset, stubs.length)];
  }
}
