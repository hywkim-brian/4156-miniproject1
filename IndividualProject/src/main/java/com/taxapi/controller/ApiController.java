package com.taxapi.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.taxapi.model.Client;
import com.taxapi.model.Item;
import com.taxapi.model.SupportedResponse;
import com.taxapi.model.TaxQuoteRequest;
import com.taxapi.model.TaxQuoteResponse;
import com.taxapi.service.TaxApiService;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for the Tax API.
 */
@RestController
@RequestMapping("/v1")
public final class ApiController {

    /** The tax API service. */
    private final TaxApiService taxApiService;


    public ApiController(
        final TaxApiService taxApiService
    ) {
        this.taxApiService = taxApiService;
    }

    /**
     * Creates a new client.
     *
     * @param client the client to create
     * @return the created client
     * @throws IOException if an I/O error occurs
     */
    @PostMapping("/clients")
    public ResponseEntity<?> createClient(
        @RequestBody final Client client
    ) throws IOException {
        Client createdClient =
            taxApiService.createClient(
                client.getName()
            );
        if (createdClient == null) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(java.util.Map.of(
                    "error",
                    "A client with that name "
                        + "already exists"
                ));
        }
        return ResponseEntity.ok(createdClient);
    }


    @PostMapping("/items")
    public ResponseEntity<Item> createItem(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @RequestBody final Item item
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        Item createdItem = taxApiService.createItem(
            item.getName(),
            item.getCategory(),
            item.getBasePrice()
        );
        return ResponseEntity.ok(createdItem)
    }


    @GetMapping("/items")
    public ResponseEntity<List<Item>> getItems(
        @RequestHeader("X-API-Key")
        final String apiKey
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        List<Item> items = taxApiService.getItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Gets an item by its ID.
     *
     * @param apiKey the API key
     * @param id the item ID
     * @return the item if found
     * @throws IOException if an I/O error occurs
     */
    @GetMapping("/items/{id}")
    public ResponseEntity<Item> getItemById(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @PathVariable final String id
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        Item item = taxApiService.getItemById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }


    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @PathVariable final String id
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        boolean deleted =
            taxApiService.deleteItem(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Calculates tax for a quote request.
     *
     * @param apiKey the API key
     * @param request the tax quote request
     * @return the tax quote response
     * @throws IOException if an I/O error occurs
     */
    @PostMapping("/tax/quote")
    public ResponseEntity<TaxQuoteResponse>
        calculateTax(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @RequestBody
        final TaxQuoteRequest request
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        TaxQuoteResponse response =
            taxApiService.calculateTax(request);
        if (response == null) {
            return ResponseEntity
                .badRequest().build();
        }
        return ResponseEntity.ok(response);
    }


    @GetMapping("/supported")
    public ResponseEntity<SupportedResponse>
        getSupported(
        @RequestHeader("X-API-Key")
        final String apiKey
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        SupportedResponse response =
            taxApiService.getSupported();
        return ResponseEntity.ok(response);
    }
}
