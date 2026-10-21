package com.octiq.test.user;

import com.octiq.test.shared.Json;
import com.octiq.test.shared.Migrations;
import com.octiq.test.shared.MiniServer;
import java.io.IOException;
import java.util.List;

public class UserServiceApp {
    public static void main(String[] args) throws IOException {
        Migrations.run();
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8081"));
        MiniServer.start(port, "/users", () -> Json.array(List.of("alice", "bob", "carol")));
    }
}
