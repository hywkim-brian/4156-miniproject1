package com.taxapi;

import com.taxapi.model.Client;
import com.taxapi.model.Item;
import com.taxapi.model.SupportedResponse;
import com.taxapi.model.TaxQuoteRequest;
import com.taxapi.model.TaxQuoteResponse;
import com.taxapi.service.TaxApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestConfig.class)
class TaxApiServiceUnitTests {

    @Autowired
    private TaxApiService service;

    @Autowired
    private LocalStorageService localStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        Files.writeString(tempDir.resolve("clients.json"),
            "[{\"id\":\"client-1\",\"name\":\"Alice\",\"apiKey\":\"valid-key\"}]");
        Files.writeString(tempDir.resolve("items.json"),
            "[{\"id\":\"item-1\",\"name\":\"Laptop\",\"category\":\"electronics\",\"basePrice\":999.99}]");
        Files.writeString(tempDir.resolve("taxrates.json"),
            "[{\"state\":\"CA\",\"category\":\"electronics\",\"rate\":0.0725},"
            + "{\"state\":\"NY\",\"category\":\"clothing\",\"rate\":0.04}]");

        localStorageService.setDirectory(tempDir);
    }

    // TODO(student): add @Test methods that exercise TaxApiService directly.
    // The `service` field above is the autowired bean under test.

    @Test
    void createClientWithNewName() throws IOException {
        Client result = service.createClient("Bob");
        assertNotNull(result);
        assertEquals("Bob", result.getName());
    }

    @Test
    void createClientWithExistingName() throws IOException {
        Client result = service.createClient("Alice");
        assertNull(result);
    }

    @Test
    void validApiKey() throws IOException {
        assertTrue(service.validateApiKey("valid-key"));
    }

    @Test
    void invalidApiKey() throws IOException {
        assertFalse(service.validateApiKey("asdfasd"));
    }

    @Test
    void nullApiKey() throws IOException {
        assertFalse(service.validateApiKey(null));
    }

    @Test
    void createItem() throws IOException {
        Item result = service.createItem(
            "keyboard",
            "electronics",
            100.00
        );

        assertNotNull(result);
        assertEquals("keyboard", result.getName());
        assertEquals("electronics", result.getCategory());
        assertEquals(100.00, result.getBasePrice());
    }

    @Test
    void getItems() throws IOException {
        List<Item> items = service.getItems();

        assertEquals(1, items.size());
        assertEquals("Laptop", items.get(0).getName());
    }


    @Test
    void getItemById() throws IOException {
        Item result = service.getItemById("item-1");
        assertNotNull(result);
        assertEquals("Laptop", result.getName());
    }

    @Test
    void getItemByIdMissingItem() throws IOException {
        assertNull(service.getItemById("adfasdf"));
    }
    
    @Test
    void deleteExisting() throws IOException {
        assertTrue(service.deleteItem("item-1"));
    }

    @Test
    void deleteMissingItem() throws IOException {
        assertFalse(service.deleteItem("asdfad"));
    }

    @Test
    void calculateTaxReturns() throws IOException {
        TaxQuoteRequest request = new TaxQuoteRequest();
        request.setState("CA");
        request.setItemId("item-1");
        request.setPrice(100.0);
        request.setCategory("electronics");
        TaxQuoteResponse response = service.calculateTax(request);
        assertNotNull(response);
        assertEquals(999.99, response.getPrice(), 0.001);
        assertEquals(0.0725, response.getTaxRate(), 0.000001);
        assertEquals(72.499275, response.getTaxAmount(), 0.001);
        assertEquals(1072.489275, response.getTotal(), 0.001);
    }

    @Test
    void getSupportedReturns() throws IOException {
        SupportedResponse response = service.getSupported();

        assertNotNull(response);
        assertTrue(response.getStates().contains("CA"));
        assertTrue(response.getCategories().contains("electronics"));
    }
}
