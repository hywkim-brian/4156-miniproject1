package com.taxapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxapi.model.Client;
import com.taxapi.model.Item;
import com.taxapi.model.SupportedResponse;
import com.taxapi.model.TaxQuoteRequest;
import com.taxapi.model.TaxQuoteResponse;
import com.taxapi.model.TaxRate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service for tax API business logic.
 */
@Service
public final class TaxApiService {

    /** The object mapper. */
    private final ObjectMapper objectMapper =
        new ObjectMapper();

    /** The storage service. */
    private final StorageService storageService;

    /**
     * Constructs the service.
     *
     * @param storageService the storage service
     */
    public TaxApiService(
        final StorageService storageService
    ) {
        this.storageService = storageService;
    }

    /**
     * Reads a list from a JSON file.
     *
     * @param <T> the element type
     * @param fileName the file name
     * @param typeRef the type reference
     * @return the list
     * @throws IOException if an I/O error occurs
     */
    private <T> List<T> readList(
        final String fileName,
        final TypeReference<List<T>> typeRef
    ) throws IOException {
        String json =
            storageService.readFile(fileName);
        return objectMapper.readValue(json, typeRef);
    }

    /**
     * Writes a list to a JSON file.
     *
     * @param <T> the element type
     * @param fileName the file name
     * @param list the list to write
     * @throws IOException if an I/O error occurs
     */
    private <T> void writeList(
        final String fileName,
        final List<T> list
    ) throws IOException {
        String json = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(list);
        storageService.writeFile(fileName, json);
    }

    /**
     * Creates a new client.
     *
     * @param name the client name
     * @return the created client or null
     * @throws IOException if an I/O error occurs
     */
    public Client createClient(
        final String name
    ) throws IOException {
        List<Client> clients = readList(
            "clients.json",
            new TypeReference<>() { }
        );

        boolean nameExists = clients.stream()
            .anyMatch(c ->
                c.getName().equalsIgnoreCase(name)
            );
        if (nameExists) {
            return null;
        }

        Client newClient = new Client(
            UUID.randomUUID().toString(),
            name,
            UUID.randomUUID().toString()
        );

        clients.add(newClient);
        writeList("clients.json", clients);

        return newClient;
    }

    /**
     * Validates an API key.
     *
     * @param apiKey the API key
     * @return true if valid
     * @throws IOException if an I/O error occurs
     */
    public boolean validateApiKey(
        final String apiKey
    ) throws IOException {
        if (apiKey == null) {
            return false;
        }
        List<Client> clients = readList(
            "clients.json",
            new TypeReference<>() { }
        );

        return clients.stream()
            .anyMatch(client ->
                client.getApiKey().equals(apiKey)
            );
    }

    /**
     * Creates a new item.
     *
     * @param name the item name
     * @param category the item category
     * @param basePrice the base price
     * @return the created item
     * @throws IOException if an I/O error occurs
     */
    public Item createItem(
        final String name,
        final String category,
        final double basePrice
    ) throws IOException {
        List<Item> items = readList(
            "items.json",
            new TypeReference<>() { }
        );

        Item newItem = new Item(
            UUID.randomUUID().toString(),
            name,
            category,
            basePrice
        );

        items.add(newItem);
        writeList("items.json", items);

        return newItem;
    }

    /**
     * Gets all items.
     *
     * @return all items
     * @throws IOException if an I/O error occurs
     */
    public List<Item> getItems() throws IOException {
        return readList(
            "items.json",
            new TypeReference<>() { }
        );
    }

    /**
     * Gets an item by ID.
     *
     * @param id the item ID
     * @return the item or null
     * @throws IOException if an I/O error occurs
     */
    public Item getItemById(
        final String id
    ) throws IOException {
        List<Item> items = readList(
            "items.json",
            new TypeReference<>() { }
        );

        return items.stream()
            .filter(item ->
                item.getId().equals(id)
            )
            .findFirst()
            .orElse(null);
    }

    /**
     * Deletes an item by ID.
     *
     * @param id the item ID
     * @return true if deleted
     * @throws IOException if an I/O error occurs
     */
    public boolean deleteItem(
        final String id
    ) throws IOException {
        List<Item> items = readList(
            "items.json",
            new TypeReference<>() { }
        );

        boolean removed = items.removeIf(item ->
            item.getId().equals(id)
        );

        if (removed) {
            writeList("items.json", items);
        }

        return removed;
    }

    /**
     * Calculates tax for a request.
     *
     * @param request the tax quote request
     * @return the tax quote response or null
     * @throws IOException if an I/O error occurs
     */
    public TaxQuoteResponse calculateTax(
        final TaxQuoteRequest request
    ) throws IOException {
        double price;
        String category;

        if (request.getItemId() != null) {
            Item item =
                getItemById(request.getItemId());
            if (item == null) {
                return null;
            }
            price = item.getBasePrice();
            category = item.getCategory();
        } else {
            price = request.getPrice();
            category = request.getCategory();
        }

        List<TaxRate> taxRates = readList(
            "taxrates.json",
            new TypeReference<>() { }
        );

        TaxRate taxRate = taxRates.stream()
            .filter(tr ->
                tr.getState().equalsIgnoreCase(
                    request.getState()
                )
            )
            .filter(tr ->
                tr.getCategory()
                    .equalsIgnoreCase(category)
            )
            .findFirst()
            .orElse(null);

        if (taxRate == null) {
            return null;
        }

        double rate = taxRate.getRate();
        double taxAmount = price * rate;
        double total = price + taxAmount;

        return new TaxQuoteResponse(
            price, rate, taxAmount, total
        );
    }

    /**
     * Gets supported states and categories.
     *
     * @return the supported response
     * @throws IOException if an I/O error occurs
     */
    public SupportedResponse getSupported()
        throws IOException {
        List<TaxRate> taxRates = readList(
            "taxrates.json",
            new TypeReference<>() { }
        );

        List<String> states = taxRates.stream()
            .map(TaxRate::getState)
            .distinct()
            .toList();

        List<String> categories = taxRates.stream()
            .map(TaxRate::getCategory)
            .distinct()
            .toList();

        return new SupportedResponse(
            states, categories
        );
    }
}
