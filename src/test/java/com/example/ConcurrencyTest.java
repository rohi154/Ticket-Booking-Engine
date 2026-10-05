package com.example.samyad;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrencyTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void testConcurrentBooking() {

        // ---------------------------------------------------------
        // STEP 1: Login
        // ---------------------------------------------------------

        String jwt = loginAndGetToken();

        System.out.println(
                "JWT LENGTH: " + jwt.length()
        );

        // ---------------------------------------------------------
        // STEP 2: Test JWT authentication with ONE request
        // ---------------------------------------------------------

        testAuthentication(jwt);

        // ---------------------------------------------------------
        // STEP 3: Send 20 concurrent booking requests
        // ---------------------------------------------------------

        int numberOfRequests = 20;

        List<CompletableFuture<HttpResponse<String>>> requests =
                new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "http://localhost:8081/api/bookings"
                    ))
                    .header(
                            "Authorization",
                            "Bearer " + jwt
                    )
                    .header(
                            "Content-Type",
                            "application/json"
                    )
                    .POST(
                            HttpRequest.BodyPublishers.ofString("""
                                    {
                                        "eventId": 16,
                                        "quantity": 1
                                    }
                                    """)
                    )
                    .build();

            CompletableFuture<HttpResponse<String>> response =
                    httpClient.sendAsync(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            requests.add(response);
        }

        CompletableFuture.allOf(
                requests.toArray(new CompletableFuture[0])
        ).join();

        // ---------------------------------------------------------
        // STEP 4: Analyze responses
        // ---------------------------------------------------------

        int successfulBookings = 0;

        for (CompletableFuture<HttpResponse<String>> request
                : requests) {

            HttpResponse<String> response = request.join();

            System.out.println(
                    "Status: " + response.statusCode()
            );

            System.out.println(
                    "Response: " + response.body()
            );

            if (response.statusCode() == 200) {
                successfulBookings++;
            }
        }

        System.out.println(
                "Successful bookings: " + successfulBookings
        );

        // With exactly one ticket, exactly one booking should succeed.
        assertEquals(
                1,
                successfulBookings,
                "Exactly one booking should succeed"
        );
    }

    // -------------------------------------------------------------
    // Login and extract JWT
    // -------------------------------------------------------------

    private String loginAndGetToken() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:8081/api/auth/login"
                ))
                .header(
                        "Content-Type",
                        "application/json"
                )
                .POST(
                        HttpRequest.BodyPublishers.ofString("""
                                {
                                    "email": "finaltest@gmail.com",
                                    "password": "password123"
                                }
                                """)
                )
                .build();

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(
                    "LOGIN STATUS: "
                            + response.statusCode()
            );

            System.out.println(
                    "LOGIN RESPONSE: "
                            + response.body()
            );

            if (response.statusCode() != 200) {

                throw new RuntimeException(
                        "Login failed: "
                                + response.body()
                );
            }

            String body = response.body();

            String marker = "\"token\":\"";

            int start = body.indexOf(marker);

            if (start == -1) {

                throw new RuntimeException(
                        "JWT token not found in login response"
                );
            }

            start += marker.length();

            int end = body.indexOf("\"", start);

            if (end == -1) {

                throw new RuntimeException(
                        "Could not find end of JWT token"
                );
            }

            String token = body.substring(start, end);

            if (token.isBlank()) {

                throw new RuntimeException(
                        "Extracted JWT token is empty"
                );
            }

            System.out.println(
                    "JWT EXTRACTED SUCCESSFULLY"
            );

            return token;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not login",
                    e
            );
        }
    }

    // -------------------------------------------------------------
    // Verify JWT authentication before concurrency test
    // -------------------------------------------------------------

    private void testAuthentication(String jwt) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:8081/api/events/test-auth"
                ))
                .header(
                        "Authorization",
                        "Bearer " + jwt
                )
                .GET()
                .build();

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(
                    "AUTH TEST STATUS: "
                            + response.statusCode()
            );

            System.out.println(
                    "AUTH TEST RESPONSE: "
                            + response.body()
            );

            assertEquals(
                    200,
                    response.statusCode(),
                    "JWT authentication failed before concurrency test"
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Authentication test request failed",
                    e
            );
        }
    }
}