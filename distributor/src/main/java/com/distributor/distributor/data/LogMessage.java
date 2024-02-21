package com.distributor.distributor.data;

import java.time.Instant;

public record LogMessage(Instant timestamp, String message, LogLevel logLevel) {}
