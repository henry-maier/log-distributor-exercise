package com.distributor.distributor.data;

import java.util.List;

public record LogPacket(List<LogMessage> logMessages) {}
