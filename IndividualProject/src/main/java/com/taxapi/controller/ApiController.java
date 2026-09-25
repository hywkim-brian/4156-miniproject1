package com.taxapi.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.taxapi.model.Client;
import com.taxapi.model.Item;
import com.taxapi.model.UpdatePriceRequest;
import com.taxapi.model.SupportedResponse;
import com.taxapi.model.TaxQuoteRequest;
import com.taxapi.model.TaxQuoteResponse;
import com.taxapi.service.TaxApiService;

import org.springframework.web.bind.annotation.PatchMapping;

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
    /**
     * Constructs an API controller.
     *
     * @param taxApiService the tax API service
     */
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
    /**
     * Creates a new item.
     *
     * @param apiKey the client API key
     * @param item the item to create
     * @return the created item
     * @throws IOException if an I/O error occurs
     */
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
        return ResponseEntity.ok(createdItem);
    }
    /**
     * Gets all items.
     *
     * @param apiKey the client API key
     * @return all items
     * @throws IOException if an I/O error occurs
     */
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
    /**
     * Deletes an item.
     *
     * @param apiKey the client API key
     * @param id the item ID
     * @return an empty response
     * @throws IOException if an I/O error occurs
     */
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
    /**
     * Gets supported states and categories.
     *
     * @param apiKey the client API key
     * @return the supported values
     * @throws IOException if an I/O error occurs
     */
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

    /**
     * Update item's base price.
     *
     * @param apiKey the client API key
     * @param id the item ID
     * @param request the update price request
     * @return the updated item
     * @throws IOException if an I/O error occurs
     */
    @PatchMapping("/items/{id}")
    public ResponseEntity<Item> updateItemPrice(
        @RequestHeader("X-API-Key")
        final String apiKey,
        @PathVariable final String id,
        @RequestBody
        final UpdatePriceRequest request
    ) throws IOException {
        if (!taxApiService.validateApiKey(apiKey)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }
        Item updatedItem = taxApiService.updateItemPrice(
            id,
            request.getBasePrice()
        );
        if (updatedItem == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedItem);
    }
}
