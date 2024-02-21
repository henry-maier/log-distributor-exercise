package com.analyzer.core;

import distributor.loganalyzer.grpc.LogAnalyzerServiceGrpc;
import distributor.loganalyzer.grpc.LogAnalyzerServiceOuterClass;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PreDestroy;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@GrpcService
public class LogAnalyzerServiceImpl extends LogAnalyzerServiceGrpc.LogAnalyzerServiceImplBase {
    private final Logger logger = LoggerFactory.getLogger(LogAnalyzerServiceImpl.class);
    private final Random r = new Random();
    private static final AtomicInteger processedLogsCount = new AtomicInteger(0);

    @Override
    public void analyzeLog(LogAnalyzerServiceOuterClass.LogPacket request,
                           StreamObserver<LogAnalyzerServiceOuterClass.AnalysisResult> responseObserver) {
        int count = processedLogsCount.incrementAndGet();
        if (count % 1000 == 0) {
            logger.info("Processed {} log packets so far", count);
        }
        // wait for some amount of time
        int waitTime = r.nextInt(50);
        logger.debug("Sleeping for {} ms", waitTime);
        try {
            TimeUnit.MILLISECONDS.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.debug("Returning success response");
        LogAnalyzerServiceOuterClass.AnalysisResult response = LogAnalyzerServiceOuterClass.AnalysisResult.newBuilder()
                .setSuccess(true)
                .setMessage("Log packet processed successfully")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @PreDestroy
    public void onShutdown() {
        logger.info("In total, processed {} log packets", processedLogsCount.get());
    }
}
