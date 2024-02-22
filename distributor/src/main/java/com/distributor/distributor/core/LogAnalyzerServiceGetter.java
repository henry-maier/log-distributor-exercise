package com.distributor.distributor.core;

import distributor.loganalyzer.grpc.LogAnalyzerServiceGrpc;
import java.util.List;
import java.util.Random;

public class LogAnalyzerServiceGetter {

  private final LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub[] stubs;
  private final float[] cumulativeWeights;
  private final Random r;

  /**
   * Constructor for LogAnalyzerServiceGetter
   *
   * @param stubs the LogAnalyzerServiceFutureStubs to use
   * @param weights the weights, in the same order as the stubs, to use to determine how often to
   *     use each stub
   */
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
    this.r = r;
  }

  /**
   * Get the LogAnalyzerServiceFutureStub to use corresponding to position
   *
   * @param position the position of the stub to get
   * @return the LogAnalyzerServiceFutureStub to use, by taking the mod of the position to be a
   *     valid index
   */
  public LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub getLogAnalyzerServiceStub(
      int position) {
    return stubs[Math.floorMod(position, stubs.length)];
  }

  /**
   * Convenience method to get a LogAnalyzerServiceFutureStub to use based on the weights
   *
   * @return the LogAnalyzerServiceFutureStub to use
   */
  public LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub getLogAnalyzerServiceStub() {
    return stubs[getWeightedPosition()];
  }

  /**
   * Get the position of the stub to use based on the weights
   *
   * @return the position of the stub to use
   */
  public int getWeightedPosition() {
    int pos = 0;
    for (float cmp = r.nextFloat(); pos < stubs.length && cmp > cumulativeWeights[pos]; pos++) {}
    return Math.floorMod(pos, stubs.length);
  }
}
