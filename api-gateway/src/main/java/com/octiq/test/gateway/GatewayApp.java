package com.octiq.test.gateway;

import com.octiq.test.shared.MiniServer;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GatewayApp {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String users = System.getenv().getOrDefault("USER_SERVICE_URL", "http://localhost:8081");
        String orders = System.getenv().getOrDefault("ORDER_SERVICE_URL", "http://localhost:8082");

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/users", ex -> proxy(ex, users));
        server.createContext("/orders", ex -> proxy(ex, orders));
        server.createContext("/health", ex -> MiniServer.respond(ex, 200, "{\"status\":\"UP\"}"));
        server.start();
        System.out.println("api-gateway listening on :" + port);
    }

    private static void proxy(com.sun.net.httpserver.HttpExchange ex, String base) throws IOException {
        try {
            String target = base + ex.getRequestURI().getPath();
            String query = ex.getRequestURI().getRawQuery();
            if (query != null) {
                target += "?" + query;
            }
            byte[] body = ex.getRequestBody().readAllBytes();
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(target))
                    .method(ex.getRequestMethod(), body.length == 0
                            ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofByteArray(body));
            String contentType = ex.getRequestHeaders().getFirst("Content-Type");
            if (contentType != null) {
                request.header("Content-Type", contentType);
            }
            HttpResponse<String> r = CLIENT.send(
                    request.build(),
                    HttpResponse.BodyHandlers.ofString());
            MiniServer.respond(ex, r.statusCode(), r.body());
        } catch (InterruptedException | IOException e) {
            MiniServer.respond(ex, 502, "{\"error\":\"upstream unavailable\"}");
        }
    }
}
