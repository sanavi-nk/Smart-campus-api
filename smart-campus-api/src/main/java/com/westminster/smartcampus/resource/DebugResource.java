package com.westminster.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Debug endpoint for demonstrating the global 500 safety-net mapper.
 * Intentionally throws a NullPointerException so we can prove the client
 * receives a clean JSON error with no stack trace leak.
 */
@Path("/debug")
@Produces(MediaType.APPLICATION_JSON)
public class DebugResource {

    @GET
    @Path("/boom")
    public String triggerUnexpectedError() {
        // Intentional NPE to demonstrate GenericThrowableMapper.
        String nothing = null;
        return nothing.toLowerCase();
    }
}
