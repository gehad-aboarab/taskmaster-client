package com.example.gehad.taskmaster;

import java.net.HttpURLConnection;

public class Response {

    public final String body;
    public final int status;

    public Response(String body, int status) {
        this.body = body;
        this.status = status;

    }

}
