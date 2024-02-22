package com.distributor.distributor.core;

import com.distributor.distributor.data.LogMessage;
import com.distributor.distributor.data.LogPacket;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Timestamp;
import distributor.loganalyzer.grpc.LogAnalyzerServiceGrpc;
import distributor.loganalyzer.grpc.LogAnalyzerServiceOuterClass;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import org.jctools.queues.MpscArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LogDistributorService {

  private static final long INITIAL_DELAY_MS = 10; // Initial delay before first retry
  private static final long MAX_DELAY_MS = 5000; // Maximum delay between retries
  private static final int BACKOFF_MULTIPLIER = 2; // Backoff multiplier for exponential backoff

  // Create a fixed-size thread pool with a single worker thread
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final Logger logger = LoggerFactory.getLogger(LogDistributorService.class);
  @Value("${queueSize}")
  private int MAX_QUEUE_SIZE;

  private final Queue<LogPacket> queue = new MpscArrayQueue<>(MAX_QUEUE_SIZE);

  @Autowired private LogAnalyzerServiceGetter logAnalyzerServiceGetter;

  /**
   * Default constructor for LogDistributorService. Starts the processing thread upon instantiation.
   */
  public LogDistributorService() {
    // Start the processing thread upon instantiation
    startProcessingThread();
  }

  /**
   * Distributes the log packet to the processing thread.
   * @param logPacket The log packet to be distributed.
   * @return true if the log packet was successfully distributed, false otherwise.
   */
  public boolean distributeLog(LogPacket logPacket) {
    // Add the log packet to the queue for processing
    return queue.offer(logPacket);
  }

  /**
   * Shuts down the LogDistributorService gracefully.
   */
  @PreDestroy
  public void onShutdown() {
    logger.info("Requesting log executor to shutdown...");
    // Shutdown the ExecutorService gracefully
    executor.shutdown();
    try {
      // Wait for tasks to complete or until timeout
      if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
        // If tasks are still running after timeout, forceful shutdown
        logger.error("Log executor did not shutdown gracefully, forcing shutdown...");
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      logger.error("Log executor did not shutdown gracefully, forcing shutdown...");
      // Restore interrupted status and proceed with shutdown
      Thread.currentThread().interrupt();
      executor.shutdownNow();
    }
  }

  private void startProcessingThread() {
    // Submit a task to the executor to process log packets
    logger.info("Starting processing thread...");
    executor.submit(
        () -> {
          try {
            while (true) {
              // Dequeue log packets from the queue
              LogPacket logPacket = queue.poll();
              if (logPacket != null) {
                // Process the log packet
                processLogPacket(logPacket);
              } else {
                // Sleep for 100 milliseconds if the queue is empty
                Thread.sleep(100);
              }
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Log distributor thread interrupted", e);
          }
        });
  }

  private void processLogPacket(LogPacket logPacket) {
    // Add your logic here to process the received log packet
    LogAnalyzerServiceOuterClass.LogPacket logPacketProto = toProto(logPacket);
    callAnalyzeLog(
        logPacketProto, logAnalyzerServiceGetter.getLogAnalyzerServiceStub(logPacketProto), 0);
  }

  private void callAnalyzeLog(
      LogAnalyzerServiceOuterClass.LogPacket logPacketProto,
      LogAnalyzerServiceGrpc.LogAnalyzerServiceFutureStub stub,
      int retries) {
    ListenableFuture<LogAnalyzerServiceOuterClass.AnalysisResult> res =
        stub.analyzeLog(logPacketProto);
    Futures.addCallback(
        res,
        new FutureCallback<>() {
          @Override
          public void onSuccess(LogAnalyzerServiceOuterClass.AnalysisResult result) {
            logger.debug("Log packet processed successfully. Response: {}", result.getMessage());
          }

          @Override
          public void onFailure(Throwable t) {
            logger.error("Error processing log packet", t);
            logger.info("RETRYING...");
            try {
              Thread.sleep(getMillis(retries));
            } catch (InterruptedException e) {
              logger.error("Thread interrupted while waiting to retry");
              throw new RuntimeException(e);
            }
            callAnalyzeLog(
                logPacketProto,
                logAnalyzerServiceGetter.getLogAnalyzerServiceStub(logPacketProto, retries + 1),
                retries + 1);
          }
        },
        MoreExecutors.directExecutor());
  }

  private long getMillis(int retries) {
    return (long) Math.min(Math.pow(BACKOFF_MULTIPLIER, retries) * INITIAL_DELAY_MS, MAX_DELAY_MS);
  }

  private LogAnalyzerServiceOuterClass.LogPacket toProto(LogPacket logPacket) {
    LogAnalyzerServiceOuterClass.LogPacket.Builder logPacketProtoBuilder =
        LogAnalyzerServiceOuterClass.LogPacket.newBuilder();
    logPacket
        .logMessages()
        .forEach(logMessage -> logPacketProtoBuilder.addLogMessages(toProto(logMessage)));
    return logPacketProtoBuilder.build();
  }

  private LogAnalyzerServiceOuterClass.LogMessage toProto(LogMessage logMessage) {
    return LogAnalyzerServiceOuterClass.LogMessage.newBuilder()
        .setMessage(logMessage.message())
        .setSeverityValue(logMessage.logLevel().ordinal())
        .setTimestamp(
            Timestamp.newBuilder()
                .setSeconds(logMessage.timestamp().getEpochSecond())
                .setNanos(logMessage.timestamp().getNano()))
        .build();
  }
}
