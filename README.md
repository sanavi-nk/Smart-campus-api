# Smart Campus Sensor & Room Management API
 
<sub>Client Server Architecture</sub>
 
<sub>Sanavi Kulathilake</sub>
 
<sub>UoW ID — w2121319 &nbsp;&nbsp;|&nbsp;&nbsp; IIT ID — 20241171</sub>
 
---
 
## Overview
 
The Smart Campus API is a RESTful web service built using JAX-RS (Jersey) and deployed on Apache Tomcat 9. It provides a comprehensive interface for managing university campus rooms, IoT sensors, and sensor reading history. The API follows REST architectural principles including resource-based URLs, appropriate HTTP status codes, structured JSON responses, and hypermedia-driven navigation (HATEOAS).
 
The system is built around three core resources. Rooms represent physical spaces on campus such as labs and libraries. Sensors are IoT devices deployed within rooms such as CO2, Temperature, and Occupancy monitors. Sensor Readings are historical measurement logs recorded by each sensor over time.
 
All data is stored in-memory using thread-safe ConcurrentHashMap structures via dedicated singleton store classes — RoomStore, SensorStore, and ReadingStore — ensuring data consistency across concurrent requests without the use of any external database.
 
---
 
## Project Structure
 
```
smart-campus-api/
├── pom.xml
├── src/main/
│   ├── java/com/westminster/smartcampus/
│   │   ├── JAXRSConfiguration.java
│   │   ├── dto/
│   │   │   └── ErrorResponse.java
│   │   ├── model/
│   │   │   ├── Room.java
│   │   │   ├── Sensor.java
│   │   │   └── SensorReading.java
│   │   ├── store/
│   │   │   ├── RoomStore.java
│   │   │   ├── SensorStore.java
│   │   │   └── ReadingStore.java
│   │   ├── resource/
│   │   │   ├── DiscoveryResource.java
│   │   │   ├── RoomResource.java
│   │   │   ├── SensorResource.java
│   │   │   ├── SensorReadingResource.java
│   │   │   └── DebugResource.java
│   │   ├── exception/
│   │   │   ├── RoomNotEmptyException.java
│   │   │   ├── LinkedResourceNotFoundException.java
│   │   │   └── SensorUnavailableException.java
│   │   ├── mapper/
│   │   │   ├── RoomNotEmptyMapper.java
│   │   │   ├── LinkedResourceNotFoundMapper.java
│   │   │   ├── SensorUnavailableMapper.java
│   │   │   └── GenericThrowableMapper.java
│   │   └── filter/
│   │       └── LoggingFilter.java
│   └── webapp/WEB-INF/
│       └── web.xml
```
 
---
 
## API Endpoints
 
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | Discovery — API metadata and resource links |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors exist) |
| GET | `/api/v1/sensors` | List all sensors (optional `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor |
| DELETE | `/api/v1/sensors/{sensorId}` | Delete a sensor |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading for a sensor |
| GET | `/api/v1/debug/boom` | Triggers a 500 error to demonstrate global exception mapper |
 
---
 
## How to Build and Run
 
### Prerequisites
 
Make sure the following are installed on your machine before proceeding.
 
Java JDK 11 or higher
 
Apache Maven 3.6 or higher
 
Apache Tomcat 9
 
NetBeans IDE or any Maven-compatible IDE
 
---
 
### Step 1 — Clone the Repository
 
```bash
git clone https://github.com/sanavi-nk/Smart-campus-api.git
cd smart-campus-api
```
 
---
 
### Step 2 — Build the Project
 
Run the following Maven command from the root of the project to compile and package it into a WAR file.
 
```bash
mvn clean package
```
 
A successful build will produce the following file inside the target folder.
 
```
target/smart-campus-api-1.0-SNAPSHOT.war
```
 
---
 
### Step 3 — Deploy to Tomcat
 
Option A — Using NetBeans (Recommended)
 
Open the project in NetBeans via File then Open Project. Right-click the project and select Run. NetBeans will automatically deploy to the configured Tomcat server.
 
 
---
 
### Step 4 — Verify the Server is Running
 
Open your browser and navigate to the following URL.
 
```
http://localhost:8080/smart-campus-api-1.0-SNAPSHOT/api/v1
```
 
You should see a JSON response containing API metadata and hypermedia links to all available resource collections.
 
---
 
## Sample curl Commands
 
### 1. Get API Metadata (Discovery)
 
```bash
curl -X GET http://localhost:8080/smart-campus-api-1.0-SNAPSHOT/api/v1 \
  -H "Accept: application/json"
```
 
Expected response is 200 OK with API version, contact information, and resource links.
 
---
 
### 2. Create a New Room
 
```bash
curl -X POST http://localhost:8080/smart-campus-api-1.0-SNAPSHOT/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "LAB-101", "name": "Computer Lab", "capacity": 30}'
```
 
Expected response is 201 Created with the full room object and a Location header pointing to the new resource.
 
---
 
### 3. Register a Sensor with a Valid roomId
 
```bash
curl -X POST http://localhost:8080/smart-campus-api-1.0-SNAPSHOT/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "CO2-001", "type": "CO2", "status": "ACTIVE", "currentValue": 400.0, "roomId": "LAB-101"}'
```
 
Expected response is 201 Created with the sensor object. The sensor is also linked to the room automatically.
 
---
 
### 4. Filter Sensors by Type
 
```bash
curl -X GET "http://localhost:8080/smart-campus-api-1.0-SNAPSHOT/api/v1/sensors?type=CO2" \
  -H "Accept: application/json"
```
 
Expected response is 200 OK with a filtered list containing only sensors of type CO2.
 
---
 
### 5. Post a Sensor Reading
 
```bash
curl -X POST http://localhost:8080/smart-campus-api-1.0-SNAPSHOT/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 450.5}'
```
 
Expected response is 201 Created with the new reading object. The parent sensor's currentValue is also updated to 450.5 as a side effect.
 
---
 
### 6. Attempt to Delete a Room That Still Has Sensors
 
```bash
curl -X DELETE http://localhost:8080/smart-campus-api-1.0-SNAPSHOT/api/v1/rooms/LAB-101 \
  -H "Accept: application/json"
```
 
Expected response is 409 Conflict with a JSON error body explaining that the room still has sensors assigned.
 
---
 
### 7. Register a Sensor with a Non-Existent roomId
 
```bash
curl -X POST http://localhost:8080/smart-campus-api-1.0-SNAPSHOT/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "Temperature", "status": "ACTIVE", "currentValue": 22.0, "roomId": "FAKE-999"}'
```
 
Expected response is 422 Unprocessable Entity with a JSON error body explaining that the referenced room does not exist.
 
---
 
### 8. Trigger the Global 500 Safety Net
 
```bash
curl -X GET http://localhost:8080/smart-campus-api-1.0-SNAPSHOT/api/v1/debug/boom \
  -H "Accept: application/json"
```
 
Expected response is 500 Internal Server Error with a clean generic JSON message and no stack trace visible to the client.
 
---
 
## Report — Question Answers

**1.1 — JAX-RS Resource Lifecycle**

The default lifecycle of a JAX-RS Resource class is, it initiates a new instance for every incoming HTTP Request. Its the opposite of a Singleton. If a client requests GET/api/v1/rooms, JAX-RS creates a brand new RoomResource object to handle the request and destroys it once the response is sent.



Because JAX-RS creates a new Resource object for each request, any data contained within this object is guaranteed to be lost as soon as the request finishes executing. It is therefore impossible to preserve any data across multiple requests by simply saving it into the Resource objects themselves. In order to get around this problem, any data that needs to be accessed across different requests must be saved in some other, non-transient location.

A good solution to this is provided by the Singleton DataStore, which is one particular DataStore object instantiated just once at the start of the application and lasting until the server itself shuts down. Any Resource object will connect to this same DataStore object during processing.



Nevertheless, storing the same set of data through all request processing gives rise to the next challenge. Since in a web application, more than one client request can occur at any moment in time and will be processed by multiple threads at once, it becomes possible to experience the race condition when two threads attempt to manipulate data simultaneously. For instance, when two clients register the sensor, it is possible to lose or overwrite their entries.

To avoid the mentioned issue, it is necessary to make sure that the utilized data structures are thread-safe. The standard Java implementations of HashMap or ArrayList cannot be considered safe since these classes were not designed with the concurrency requirement in mind. To ensure thread safety, it is proposed to implement the store using the special implementation of the data structure – ConcurrentHashMap.



**1.2 — HATEOAS and Hypermedia in RESTful Design**

"HATEOAS" refers to "Hypermedia As The Engine Of Application State." It is often regarded as one of the most advanced and comprehensive forms of REST architecture, first outlined by Roy Fielding in his PhD dissertation defining the term REST. The basic concept here is that a response from an API is not limited to returning data; it must provide links and navigational paths to inform the client about which further actions it can take and how.

For example, in the Smart Campus API, we have the discovery resource at **GET /api/v1**, which returns version and contact details as well as providing a navigation path for resource collections like /api/v1/rooms and /api/v1/sensors. The implication is that if an API consumer makes a request to the endpoint for the very first time, it will be able to find out everything it needs by just sending one request, without referring to any external documentation.



The use of HATEOAS in developing a REST API makes such an API essentially self-explanatory. In each response, there is always some information on what actions can be performed next based on the current resource. For instance, having created a new room, the response may contain links to view more details about the room, link to a page where the sensors in the room can be added, and another link to the list of all the rooms. There is no need for any prior knowledge of the URL addresses to perform all these actions; rather, they are explicitly mentioned in the response.

An API becomes far more than just a way to exchange data but approaches being a navigation-based tool similar to navigating the web through hyperlinks using the browser.



Static documentation like PDF files, wikis, or lists of hardcoded URLs introduces a brittle relationship between the client and server. Whenever a URL scheme, an endpoint name, or other resources get changed on the server, all the clients that were relying on static documentation have to be manually modified to accommodate the new state of affairs. Such a setup is very costly in terms of maintenance and poses a risk of breaking the integration process entirely.

However, HATEOAS removes several limitations associated with static documentation. First, it allows for documenting the API through responses rather than separate documents. In other words, HATEOAS makes the API self-descriptive: the API responses provide up-to-date information about the server configuration. Second, it decreases client coupling, thus eliminating the need for hardcoded URLs. Third, it provides better discoverability for new contributors who can explore the API just by following links starting from the root endpoint. Fourth, it makes the API more resilient to changes: once the link on the server side gets updated, all the clients will be updated automatically due to the link.



**2.1 — Returning IDs Only vs Full Room Objects**

In determining how to design the **GET /api/v1/rooms**, there are important decisions that need to be made on whether the endpoints will include only room IDs or complete objects that contain all properties like name, capacity, and sensor assignments.



IDs Only

An endpoint that includes only the IDs will produce a very lightweight response that reduces bandwidth usage considerably. While this will be useful when thousands of rooms are returned, it means the client will have to make another **HTTP request per each room ID** in order to get the room details. This means N+1 request will happen where there is one initial request for the list and then **N additional** requests for each record.



Full Objects

Using the endpoint to include room objects will help the client receive all information about the rooms in one single request, thus solving the N+1 request problem. While there will be an increase in the response payload, the bandwidth usage becomes an acceptable cost since facilities managers and other automated applications will need the full information immediately.



**2.2 — Idempotency of the DELETE Operation**

Idempotency of the DELETE Operation

A call is considered idempotent if **calling it several times** yields the same outcome as doing it only once. DELETE operations are specified as idempotent by the HTTP specification, which implies that repeating a call should not yield inconsistent results or raise exceptions on the server side.



Implementation

In the current API, **DELETE /api/v1/rooms/{roomId}** is designed to be completely idempotent in terms of its implementation.

First, if the user sends a DELETE request for an existing room which does not have any sensors, then the room will be deleted from the data storage and the server will return a status code **204 No Content**, meaning that the operation was performed successfully.

Second, when the user sends the same DELETE request, the room in question does not exist in the database anymore, therefore the server returns a **204 No Content** response once again. As you see, the behavior of the server is exactly the same in both cases and the room does not exist in either case, which precisely describes what DELETE operation is supposed to achieve. Consequently, the server's state stays intact regardless of how many times DELETE is called.



Rooms With Active Sensors

However, there is one situation where the deletion request would be deliberately refused. The room will not allow the DELETE method until all of its sensors have been detached from it. This is why the DELETE fails in this instance and generates a response with status **409 Conflict** containing an appropriate JSON object describing the problem. This does not affect idempotency in any way, because this requirement is enforced before the first call to the DELETE operation is even made.



**3.1 — @Consumes Media Type Mismatch**

**@Consumes(MediaType.APPLICATION\_JSON)** is an annotation provided by JAX-RS that tells the framework that the **POST /api/v1/sensors** endpoint can only receive request payloads of **Content-Type: application/json.**

In case of a request from a client in other formats like **text/plain** or **application/xml**, JAX-RS does not allow the request payload to reach the method at all. JAX-RS checks whether the type of request received matches what is specified in the **@Consumes** annotation. In case there is a mismatch in the two, then JAX-RS automatically rejects the request and responds with an error code of **HTTP 415 Unsupported Media Type**.

This is a great internal safeguard. It guarantees that whatever data arrives at the resource method will be in the correct format only. In case of any incorrect formatting of the payload, then the framework prevents the payload from reaching the business logic layer. It also makes sure that the client knows very well what the correct content type is for sending data to the REST resource.



**3.2 — QueryParam vs PathParam for Filtering**

**QueryParam vs PathParam for Filtering**

In the implementation of the API, the collection of sensors is filtered based on its type through the use of a query parameter **(@QueryParam)** mechanism, leading to an endpoint URL like **GET /api/v1/sensors?type=CO2**. An alternate way to achieve this purpose could be by using path parameters (**@PathParam**), which leads to an endpoint URL similar to **GET /api/v1/sensors/type/CO2.**

There are many compelling reasons for choosing the query parameter way for searching or filtering a collection of entities.

For one thing, query parameters are optional by nature. In the above example, when the **type parameter** is not specified, all the sensors should be returned by default. Otherwise, if a path parameter technique is adopted, an entirely new endpoint should be created to cater to both scenarios.

Secondly, path parameters are used for identifying specific resources, like the following: GET **/api/v1/sensors/TEMP-001**. By incorporating a filter into the path parameters, the semantic meaning of the URLs gets distorted, suggesting that "type" or "CO2" are part of the entity name.

Thirdly, it is much easier to incorporate several filter criteria within a single request through query parameters, like this: **GET /api/v1/sensors?type=CO2\&status=ACTIVE**.

Finally, query parameters align with established REST conventions and are universally understood by client developers, API gateways, caching systems, and documentation tools, making the API more intuitive and interoperable.



**4.1 — Sub-Resource Locator Pattern**

Sub-Resource Locator Pattern

The sub-resource locator pattern implies delegating handling a nested URL to another resource class and not creating the logic of all endpoints inside one controller. In this API, when a request is made to **/api/v1/sensors/{sensorId}/readings**, the **SensorResource** doesn't deal with the readings logic inside. Instead, it will delegate it to the **SensorReadingResource** class that manages the readings' history of this particular sensor.

There are several advantages of using the above architectural solution for managing large APIs.

First of all, it ensures a proper separation of responsibilities. The idea here is that one resource class deals with one thing only, so it's easy to see the purpose of each of them. It is obvious what kind of functionality is implemented in **SensorResource** and what kind of functionality is done by **SensorReadingResource.**

Secondly, it ensures the code scalability and makes it easier to work with large APIs. In case of a dozen of nested resources, trying to create all endpoints inside one controller would lead to unmanageable files that would be complicated to debug and test.

Thirdly, it makes code more testable because each class could be tested separately without having to build the entire parent resource hierarchy.



**5.1  — Why HTTP 422 is Better Than 404 for Missing References**

If the client makes a **POST** request to register a new sensor that does not belong to an existing room (there is no corresponding entry with the provided **roomId** in the database), then the most adequate status code is **HTTP 422 Unprocessable Entity**, and not HTTP 404 Not Found, which is typically used in such cases.

To clarify, **HTTP 404** means that the provided **URL or endpoint does not exist** on the server. In the current case, everything is alright with the **POST /api/v1/sensors** endpoint: it was successfully located, the body of the request has been properly parsed, and the meaning behind the provided message has been fully recognized by the server. What happens is that the content of the request is wrong, as it includes some reference to a non-existing object in the system.

On the other hand, the 422 HTTP status code is precisely tailored for such cases, where the meaning behind the data sent to the endpoint has violated some business rules and therefore cannot be processed further. The server recognizes the request, understands its content, but cannot execute the command, because one of the objects required for processing this command does not exist in the system.



**5.2 — Cybersecurity Risks of Exposing Stack Traces**

The exposure of raw Java stack traces to the outside API clients is a significant cybersecurity concern, helping attackers penetrate the system in various ways.

First of all, a stack trace usually exposes the whole structure of the application from a programming perspective, such as file names, package names, classes, methods, and even lines in the code, providing attackers with enough information to get inside the architecture of the application.

Secondly, a stack trace also exposes names and versions of the third-party frameworks used in the project, such as Jersey, Jackson, and Tomcat. In other words, it gives an attacker enough time to find out what Common Vulnerabilities and Exposures (CVEs) are applicable to specific versions of those frameworks and create targeted vulnerabilities and exploits accordingly.

In addition, stack traces might disclose the information regarding the business logic of the application and data flow process because one can easily read what methods were called and how they interact, where certain information is stored and what kind of error was reported.



**5.3 — Why Filters are Better Than Manual Logging**

A filter used to log the messages through a JAX-RS API is far better than adding **Logger.info()** messages to each resource method because there are several reasons for choosing the former over the latter.

First, logging is considered a **cross-cutting concern**, which means that it applies to all requests and responses, independent of the endpoint involved. A filter will implement this concern in one place, ensuring that the logic is applied throughout the entire API without being duplicated in dozens of methods.

Second, filters provide **cleaner maintenance** capabilities when compared to implementing the same concern directly in resource methods. For instance, should any changes need to be done to the logging format, it is possible to simply modify the filter's code rather than searching for all resource methods that use logging.

Third, using filters will ensure that resource methods are left intact, allowing the focus of each resource method to remain unchanged. Resource methods should be responsible for handling business logic only and not any other concern, including logging messages.

Fourth, filters operate at the framework level, guaranteeing that the filter will be executed for each message regardless of whether a developer has forgotten to include the logging mechanism into a new resource method.

