package com.example.iotalarmcopilot.mockdevice.infrastructure.lwm2m;

import com.example.iotalarmcopilot.mockdevice.application.port.Lwm2mServerHandler;
import com.example.iotalarmcopilot.mockdevice.application.port.Lwm2mServerRuntime;
import com.example.iotalarmcopilot.mockdevice.config.Lwm2mGatewayConfig;
import com.example.iotalarmcopilot.mockdevice.domain.InvalidReportIntervalException;
import com.example.iotalarmcopilot.mockdevice.domain.Lwm2mDeviceSnapshot;
import com.example.iotalarmcopilot.mockdevice.domain.SetReportIntervalCommandPayload;
import org.eclipse.leshan.core.endpoint.Protocol;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.observation.CompositeObservation;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.observation.SingleObservation;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.request.exception.InvalidRequestException;
import org.eclipse.leshan.core.response.ObserveCompositeResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.LeshanServer;
import org.eclipse.leshan.server.LeshanServerBuilder;
import org.eclipse.leshan.server.californium.endpoint.CaliforniumServerEndpointsProvider;
import org.eclipse.leshan.server.model.StandardModelProvider;
import org.eclipse.leshan.server.observation.ObservationListener;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.time.OffsetDateTime;
import java.util.Collection;

public class LeshanLwm2mServer implements Lwm2mServerRuntime {

    private static final long PUBLISH_DEBOUNCE_MS = 150L;
    private static final long DUPLICATE_SUPPRESSION_WINDOW_MS = 5000L;
    // 采集资源路径
    private static final String TEMPERATURE_PATH = "/3303/0/5700";
    private static final String HUMIDITY_PATH = "/3304/0/5700";
    // 上报的资源映射
    private static final int REPORT_INTERVAL_OBJECT_ID = 31024;
    private static final int REPORT_INTERVAL_INSTANCE_ID = 0;
    private static final int REPORT_INTERVAL_RESOURCE_ID = 1;

    private final Lwm2mGatewayConfig config;
    private final Lwm2mServerHandler eventHandler;

    private LeshanServer server;
    private volatile boolean running = false;

    public LeshanLwm2mServer(Lwm2mGatewayConfig config, Lwm2mServerHandler eventHandler) {
        this.config = config;
        this.eventHandler = eventHandler;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        createServer();
    }

    @Override
    public void setReportInterval(SetReportIntervalCommandPayload command) throws InvalidReportIntervalException {
        if (server == null) {
            throw new InvalidReportIntervalException("gateway server not started");
        }

        Registration registration = server.getRegistrationService().getByEndpoint(command.deviceId());
        if (registration == null) {
            // ignore
            return;
        }

        WriteResponse response;
        try {
            response = server.send(
                    registration,
                    new WriteRequest(
                            REPORT_INTERVAL_OBJECT_ID,
                            REPORT_INTERVAL_INSTANCE_ID,
                            REPORT_INTERVAL_RESOURCE_ID,
                            (long) command.params().intervalMs()),
                    5000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("lwm2m write interrupted", exception);
        }

        if (response == null) {
            throw new InvalidReportIntervalException("lwm2m write returned null response");
        }
        if (!response.isSuccess()) {
            throw new InvalidReportIntervalException("lwm2m write failed: " + response.getCode());
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.destroy();
            server = null;
        }
    }

    private void createServer() {
        // server
        CaliforniumServerEndpointsProvider endpointsProvider =
                new CaliforniumServerEndpointsProvider.Builder()
                        .addEndpoint(new InetSocketAddress(config.bindHost(), config.bindPort()), Protocol.COAP)
                        .build();
        LeshanServerBuilder builder = new LeshanServerBuilder();
        builder.setEndpointsProviders(endpointsProvider);
        builder.setObjectModelProvider(new StandardModelProvider());
        server = builder.build();

        // 监听client的注册事件
        server.getRegistrationService().addListener(new RegistrationListener() {
            @Override
            public void registered(Registration registration, Registration previousReg,
                                   Collection<Observation> previousObservations) {
                System.out.printf("lwm2m client registered endpoint=%s address=%s%n",
                        registration.getEndpoint(), registration.getSocketAddress());
                eventHandler.onClientRegistered(registration.getEndpoint());
                observeMetric(registration, TEMPERATURE_PATH);
                observeMetric(registration, HUMIDITY_PATH);
            }

            @Override
            public void updated(org.eclipse.leshan.server.registration.RegistrationUpdate update,
                                Registration updatedRegistration,
                                Registration previousRegistration) {
                System.out.printf("lwm2m client updated endpoint=%s%n", updatedRegistration.getEndpoint());
            }

            @Override
            public void unregistered(Registration registration,
                                     Collection<Observation> observations,
                                     boolean expired,
                                     Registration newReg) {
                System.out.printf("lwm2m client unregistered endpoint=%s expired=%s%n",
                        registration.getEndpoint(), expired);
            }
        });

        // 监听client的资源观察事件
        server.getObservationService().addListener(new ObservationListener() {
            @Override
            public void newObservation(Observation observation, Registration registration) {
                if (observation instanceof SingleObservation singleObservation) {
                    System.out.printf("lwm2m observation created endpoint=%s path=%s%n",
                            registration.getEndpoint(),
                            singleObservation.getPath());
                }
            }

            @Override
            public void cancelled(Observation observation) {
                System.out.printf("lwm2m observation cancelled registrationId=%s%n", observation.getRegistrationId());
            }

            @Override
            public void onResponse(SingleObservation observation, Registration registration, ObserveResponse response) {
                if (!response.isSuccess()) {
                    System.out.printf("lwm2m observe failed endpoint=%s path=%s%n",
                            registration.getEndpoint(),
                            observation.getPath());
                    return;
                }

                LwM2mResource resource = asSingleResource(response);
                if (resource == null) {
                    return;
                }
                BigDecimal value = toDecimal(resource.getValue());
                if (value == null) {
                    return;
                }

                String path = observation.getPath().toString();
                Lwm2mDeviceSnapshot current = Lwm2mDeviceSnapshot.empty(registration.getEndpoint());
                OffsetDateTime now = OffsetDateTime.now();
                Lwm2mDeviceSnapshot updated = switch (path) {
                    case TEMPERATURE_PATH -> current.withTemperature(value, now);
                    case HUMIDITY_PATH -> current.withHumidity(value, now);
                    default -> current;
                };
                eventHandler.onTelemetryReported(updated.deviceId(), updated);
            }

            @Override
            public void onResponse(CompositeObservation observation, Registration registration,
                                   ObserveCompositeResponse response) {
                System.out.printf("lwm2m composite observe ignored endpoint=%s%n", registration.getEndpoint());
            }

            @Override
            public void onError(Observation observation, Registration registration, Exception error) {
                System.out.printf("lwm2m observe error endpoint=%s reason=%s%n",
                        registration.getEndpoint(),
                        error.getMessage());
            }
        });
    }

    private void observeMetric(Registration registration, String path) {
        try {
            server.send(
                    registration,
                    new ObserveRequest(path),
                    5000L);
            System.out.printf("lwm2m observe requested endpoint=%s path=%s%n", registration.getEndpoint(), path);
        } catch (InvalidRequestException exception) {
            throw new IllegalStateException("invalid observe path " + path, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("observe request interrupted", exception);
        }
    }

    private LwM2mResource asSingleResource(ObserveResponse response) {
        if (response.getContent() instanceof LwM2mResource resource) {
            return resource;
        }
        if (response.getContent() instanceof LwM2mSingleResource singleResource) {
            return singleResource;
        }
        return null;
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
