package com.example.iotalarmcopilot.mockdevice.application.port;

import com.example.iotalarmcopilot.mockdevice.domain.InvalidReportIntervalException;
import com.example.iotalarmcopilot.mockdevice.domain.SetReportIntervalCommandPayload;

public interface Lwm2mServerRuntime {

    void start();

    boolean setReportInterval(SetReportIntervalCommandPayload payload) throws InvalidReportIntervalException;

    void stop();

}
