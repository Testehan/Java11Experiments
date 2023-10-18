package com.testehan.httpclient;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class App
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        System.out.println( "Hello World!" );

        HttpClient client = HttpClient.newBuilder()
           .version(HttpClient.Version.HTTP_2)     // this is the default anyway, so this line is not really needed
           .followRedirects(HttpClient.Redirect.NORMAL) // means to always follow redirects, unless you are being redirected from the more secure HTTPS to HTTP
           .connectTimeout(Duration.ofSeconds(20))
           .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                .timeout(Duration.ofMinutes(1))
                .build();

        // Synchronous
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // BodyHandlers.ofString() is a factory that creates body handler that accumulates the response body bytes and returns them as String
        System.out.println("Response obtained.");
        System.out.println(response.statusCode());
        System.out.println(response.body());

        //Asynchronous
        CompletableFuture<HttpResponse<String>> responseAsync = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response may not be obtained at this point.");

        responseAsync.thenApply(resp -> {
                        System.out.println(resp.statusCode());
                        return resp;
                    })
                    .thenApply(HttpResponse::body)
                    .thenAccept(System.out::println);

        Thread.sleep(5000); // added so that the async request has time to complete

        // parse the json into objects
        ObjectMapper objectMapper = new ObjectMapper();
        List<Post> posts = objectMapper.readValue(response.body(), new TypeReference<List<Post>>() {});
        System.out.println("Printing all Post objects");
        posts.forEach(System.out::println);
    }
}
