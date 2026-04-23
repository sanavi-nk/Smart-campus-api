package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.dto.ErrorResponse;
import com.westminster.smartcampus.exception.SensorUnavailableException;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.model.SensorReading;
import com.westminster.smartcampus.store.ReadingStore;
import com.westminster.smartcampus.store.SensorStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final SensorStore sensorStore = SensorStore.getInstance();
    private final ReadingStore readingStore = ReadingStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        Sensor sensor = sensorStore.findById(sensorId);
        if (sensor == null) {
            throw notFound("Sensor with id '" + sensorId + "' does not exist.");
        }
        List<SensorReading> history = readingStore.findBySensorId(sensorId);
        return Response.ok(history).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = sensorStore.findById(sensorId);
        if (sensor == null) {
            throw notFound("Sensor with id '" + sensorId + "' does not exist.");
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            // Thrown -> intercepted by SensorUnavailableMapper -> becomes 403 JSON.
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        if (reading == null) {
            throw badRequest("Request body must contain a reading payload.");
        }

        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0L) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        readingStore.add(sensorId, reading);
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }

    // ----- helpers -----

    private WebApplicationException notFound(String message) {
        return new WebApplicationException(
                Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ErrorResponse(404, "Not Found", message))
                        .build());
    }

    private WebApplicationException badRequest(String message) {
        return new WebApplicationException(
                Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ErrorResponse(400, "Bad Request", message))
                        .build());
    }
}
