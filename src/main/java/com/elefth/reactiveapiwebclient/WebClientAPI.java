package com.elefth.reactiveapiwebclient;

import com.elefth.reactiveapiwebclient.model.Product;
import com.elefth.reactiveapiwebclient.model.ProductEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:george@elefth.com">Eleftheriadis Georgios</a>
 */
public class WebClientAPI {

    private WebClient webClient;

    public WebClientAPI() {
//        this.webClient = WebClient.create("http://localhost:8080/products");
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080/products")
                .build();
    }

    public static void main(String[] args) {
        WebClientAPI webClient = new WebClientAPI();

        webClient.postNewProduct()
                .thenMany(webClient.getAllProducts())
                .take(1)
                .flatMap(p -> webClient.updateProduct(p.getId(), "White Tea", 0.99))
                .flatMap(p -> webClient.deleteProduct(p.getId()))
                .thenMany(webClient.getAllProducts())
                .thenMany(webClient.getAllProductEvents())
//                .take(20)
                .subscribe(System.out::println);
    }

    private Mono<ResponseEntity<Product>> postNewProduct() {
        return webClient
                .post()
                .body(Mono.just(new Product(null, "Black Tea", 1.99)), Product.class)
                .exchange()
                .flatMap(clientResponse -> clientResponse.toEntity(Product.class))
                .doOnSuccess(o -> System.out.println("********POST " + o));
    }

    private Flux<Product> getAllProducts() {
        return webClient
                .get()
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(o -> System.out.println("********GET " + o));
    }

    private Mono<Product> updateProduct(String id, String name, Double price) {
        return webClient
                .put()
                .uri("/{id}", id)
                .body(Mono.just(new Product(null, name, price)), Product.class)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnSuccess(o -> System.out.println("********UPDATE " + o));
    }

    private Mono<Void> deleteProduct(String id) {
        return webClient
                .delete()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(o -> System.out.println("********DELETE " + o));
    }

    private Flux<ProductEvent> getAllProductEvents() {
        return webClient
                .get()
                .uri("/events")
                .retrieve()
                .bodyToFlux(ProductEvent.class);
//                .doOnNext(o -> System.out.println("********GET EVENT " + o));
    }

}
