package com.octiq.test.order;

import com.octiq.test.shared.Json;
import com.octiq.test.shared.Migrations;
import com.octiq.test.shared.MiniServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderServiceApp {
    private static final List<String> ORDERS = List.of("order-1001", "order-1002");
    private static final Map<String, List<String>> LINE_ITEMS = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        Migrations.run();
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8082"));

        LINE_ITEMS.put("order-1001", new ArrayList<>(List.of("line-1001", "line-1002")));
        LINE_ITEMS.put("order-1002", new ArrayList<>(List.of("line-1003")));

        MiniServer.start(port, "/orders", OrderServiceApp::handle);
    }

    private static void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String[] parts = ex.getRequestURI().getPath().split("/");

        if (method.equals("GET") && parts.length == 2) {
            MiniServer.respond(ex, 200, Json.array(ORDERS));
            return;
        }

        if (parts.length >= 4 && parts[1].equals("orders") && parts[3].equals("line-items")) {
            String orderId = parts[2];
            List<String> items = LINE_ITEMS.get(orderId);
            if (items == null) {
                MiniServer.respond(ex, 404, "{\"error\":\"order not found\"}");
                return;
            }

            if (method.equals("GET") && parts.length == 4) {
                List<String> snapshot;
                synchronized (items) {
                    snapshot = new ArrayList<>(items);
                }
                MiniServer.respond(ex, 200, Json.array(snapshot));
                return;
            }

            if (method.equals("PUT") && parts.length == 4) {
                List<String> updated = Json.parseArray(MiniServer.body(ex));
                List<String> snapshot;
                synchronized (items) {
                    items.clear();
                    items.addAll(updated);
                    snapshot = new ArrayList<>(items);
                }
                MiniServer.respond(ex, 200, Json.array(snapshot));
                return;
            }

            if (method.equals("POST") && parts.length == 4) {
                String item = Json.parseItem(MiniServer.body(ex));
                if (item == null || item.isEmpty()) {
                    MiniServer.respond(ex, 400, "{\"error\":\"line item required\"}");
                    return;
                }
                List<String> snapshot;
                synchronized (items) {
                    items.add(item);
                    snapshot = new ArrayList<>(items);
                }
                MiniServer.respond(ex, 201, Json.array(snapshot));
                return;
            }

            if (method.equals("PUT") && parts.length == 5) {
                String item = Json.parseItem(MiniServer.body(ex));
                if (item == null || item.isEmpty()) {
                    MiniServer.respond(ex, 400, "{\"error\":\"line item required\"}");
                    return;
                }
                List<String> snapshot;
                synchronized (items) {
                    int index = items.indexOf(parts[4]);
                    if (index < 0) {
                        snapshot = null;
                    } else {
                        items.set(index, item);
                        snapshot = new ArrayList<>(items);
                    }
                }
                if (snapshot == null) {
                    MiniServer.respond(ex, 404, "{\"error\":\"line item not found\"}");
                } else {
                    MiniServer.respond(ex, 200, Json.array(snapshot));
                }
                return;
            }

            if (method.equals("DELETE") && parts.length == 4) {
                List<String> snapshot;
                synchronized (items) {
                    items.clear();
                    snapshot = new ArrayList<>(items);
                }
                MiniServer.respond(ex, 200, Json.array(snapshot));
                return;
            }

            if (method.equals("DELETE") && parts.length == 5) {
                boolean removed;
                List<String> snapshot;
                synchronized (items) {
                    removed = items.remove(parts[4]);
                    snapshot = new ArrayList<>(items);
                }
                if (removed) {
                    MiniServer.respond(ex, 200, Json.array(snapshot));
                } else {
                    MiniServer.respond(ex, 404, "{\"error\":\"line item not found\"}");
                }
                return;
            }

            MiniServer.respond(ex, 405, "{\"error\":\"method not allowed\"}");
            return;
        }

        MiniServer.respond(ex, 404, "{\"error\":\"not found\"}");
    }
}
