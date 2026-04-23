package com.westminster.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root discovery endpoint.
 * GET /api/v1  -> API metadata + hypermedia links to primary collections.
 */
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiMetadata() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("api", "Smart Campus Sensor & Room Management API");
        metadata.put("version", "1.0.0");
        metadata.put("description",
                "RESTful service for managing campus rooms, sensors, and sensor reading history.");

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("team", "Smart Campus Platform Team");
        contact.put("email", "[email protected]");
        contact.put("module", "5COSC022W - Client-Server Architectures");
        metadata.put("contact", contact);

        // HATEOAS: links to primary resource collections
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        links.put("sensorsByType", "/api/v1/sensors?type={type}");
        links.put("readings", "/api/v1/sensors/{sensorId}/readings");
        metadata.put("_links", links);

        return Response.ok(metadata).build();
    }
}
