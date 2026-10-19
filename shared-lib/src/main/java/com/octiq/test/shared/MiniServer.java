package com.octiq.test.shared;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public final class MiniServer {
    private MiniServer() { }

    public static HttpServer start(int port, String path, Supplier<String> body) throws IOException {
        return start(port, path, ex -> respond(ex, 200, body.get()));
    }

    public static HttpServer start(int port, String path, HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(path, handler);
        server.createContext("/health", ex -> respond(ex, 200, "{\"status\":\"UP\"}"));
        server.start();
        System.out.println("Listening on :" + port + " " + path);
        return server;
    }

    public static String body(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static void respond(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}
