package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.dto.ErrorResponse;
import com.westminster.smartcampus.exception.LinkedResourceNotFoundException;
import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.store.RoomStore;
import com.westminster.smartcampus.store.SensorStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.Collection;
import java.util.UUID;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final SensorStore sensorStore = SensorStore.getInstance();
    private final RoomStore roomStore = RoomStore.getInstance();

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> result;
        if (type != null && !type.isBlank()) {
            result = sensorStore.findByType(type);
        } else {
            result = sensorStore.findAll();
        }
        return Response.ok(result).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorStore.findById(sensorId);
        if (sensor == null) {
            throw notFound("Sensor with id '" + sensorId + "' does not exist.");
        }
        return Response.ok(sensor).build();
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null || sensor.getType() == null || sensor.getType().isBlank()) {
            throw badRequest("Field 'type' is required.");
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw badRequest("Field 'roomId' is required.");
        }

        Room room = roomStore.findById(sensor.getRoomId());
        if (room == null) {
            // Thrown -> intercepted by LinkedResourceNotFoundMapper -> becomes 422 JSON.
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        if (sensor.getId() == null || sensor.getId().isBlank()) {
            sensor.setId("SENSOR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        if (sensorStore.exists(sensor.getId())) {
            throw conflict("A sensor with id '" + sensor.getId() + "' already exists.");
        }

        sensorStore.save(sensor);

        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        URI location = UriBuilder.fromUri(uriInfo.getAbsolutePath())
                .path(sensor.getId())
                .build();

        return Response.created(location).entity(sensor).build();
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorStore.findById(sensorId);
        if (sensor == null) {
            throw notFound("Sensor with id '" + sensorId + "' does not exist.");
        }

        Room room = roomStore.findById(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        sensorStore.delete(sensorId);
        return Response.noContent().build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
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

    private WebApplicationException conflict(String message) {
        return new WebApplicationException(
                Response.status(Response.Status.CONFLICT)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ErrorResponse(409, "Conflict", message))
                        .build());
    }
}
