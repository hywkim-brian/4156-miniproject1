package com.taxapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.bind.annotation.RequestParam;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;


import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
@Import(TestConfig.class)
class ApiControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private LocalStorageService localStorageService;

    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    protected static final String VALID_KEY = "valid-key";

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        Files.writeString(tempDir.resolve("clients.json"),
            "[{\"id\":\"client-1\",\"name\":\"Alice\",\"apiKey\":\"valid-key\"}]");
        Files.writeString(tempDir.resolve("items.json"),
            "[{\"id\":\"item-1\",\"name\":\"Laptop\",\"category\":\"electronics\",\"basePrice\":999.99}]");
        Files.writeString(tempDir.resolve("taxrates.json"),
            "[{\"state\":\"CA\",\"category\":\"electronics\",\"rate\":0.0725},"
            + "{\"state\":\"NY\",\"category\":\"clothing\",\"rate\":0.04}]");

        localStorageService.setDirectory(tempDir);
    }

    // TODO(student): add @Test methods that exercise ApiController endpoints
    // via mockMvc.perform(...). Aim for >= 55% JaCoCo coverage overall.

    @Test
    void createClient() throws Exception {
        mockMvc.perform(post("/v1/clients")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"Bob\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bob"));
    }

    @Test
    void duplicateClient() throws Exception {
        mockMvc.perform(post("/v1/clients")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"Alice\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getItemsWithValidKey() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("item-1"))
            .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getItemsWithInvalidKey()
        throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", "bad-key"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getItemsWithoutKey()
        throws Exception {
        mockMvc.perform(get("/v1/items"))
        .andExpect(status().isBadRequest());
    }

    @Test
    void createItemWithValidKey()
        throws Exception {
        mockMvc.perform(post("/v1/items")
                .header("X-API-Key", VALID_KEY)
                .contentType(APPLICATION_JSON)
                .content(
                    "{\"name\":\"Phone\","
                        + "\"category\":\"electronics\","
                        + "\"basePrice\":500}"
                ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Phone"))
            .andExpect(jsonPath("$.category")
                .value("electronics"));
    }
    @Test
    void createItemWithInvalidKey()
        throws Exception {
        mockMvc.perform(post("/v1/items")
                .header("X-API-Key", "bad-key")
                .contentType(APPLICATION_JSON)
                .content(
                    "{\"name\":\"Phone\","
                        + "\"category\":\"electronics\","
                        + "\"basePrice\":500}"
                ))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getExistingItem() throws Exception {
        mockMvc.perform(get("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("item-1"))
            .andExpect(jsonPath("$.name").value("Laptop"));
    }


    @Test
    void getMissingItem() throws Exception {
        mockMvc.perform(get("/v1/items/missing")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isNotFound());
    }
    @Test
    void deleteExistingItem()
        throws Exception {
        mockMvc.perform(delete("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isNoContent());
    }

    @Test
    void updateItemPrice()
        throws Exception {
        mockMvc.perform(patch("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY)
                .contentType(APPLICATION_JSON)
                .content("{\"basePrice\":799.99}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("item-1"))
            .andExpect(jsonPath("$.name").value("Laptop"))
            .andExpect(jsonPath("$.category")
                .value("electronics"))
            .andExpect(jsonPath("$.basePrice")
                .value(799.99));
    }

    @Test
    void updateItemPriceWithInvalidKey()
        throws Exception {
        mockMvc.perform(patch("/v1/items/item-1")
                .header("X-API-Key", "blahblahblah")
                .contentType(APPLICATION_JSON)
                .content("{\"basePrice\":799.99}"))
            .andExpect(status().isUnauthorized());
    }


    @Test
    void updateItemPriceWithNegativePrice()
        throws Exception {
        mockMvc.perform(patch("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY)
                .contentType(APPLICATION_JSON)
                .content("{\"basePrice\":-10.00}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateMissingItem()
        throws Exception {
        mockMvc.perform(patch("/v1/items/missing")
                .header("X-API-Key", VALID_KEY)
                .contentType(APPLICATION_JSON)
                .content("{\"basePrice\":799.99}"))
            .andExpect(status().isNotFound());
        
    }

    @Test
    void getItemsByCategory()
        throws Exception {
        mockMvc.perform(get("/v1/items")
                .param("category", "ELECTRONICS")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getItemsByName()
        throws Exception {
        mockMvc.perform(get("/v1/items")
                .param("q", "top")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Laptop"));
    }


    @Test
    void getItemsByBothCategoryAndName()
        throws Exception {
        mockMvc.perform(get("/v1/items")
                .param("category", "electronics")
                .param("q", "lap")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Laptop"));
    }


    @Test
    void getItemsWithNoCategoryMatches()
        throws Exception {
        mockMvc.perform(get("/v1/items")
                .param("category", "clothing")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getItemsWithNoNameMatches()
        throws Exception {
        mockMvc.perform(get("/v1/items")
                .param("q", "phone")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    void filteredItemsWithInvalidKey()
        throws Exception {
        mockMvc.perform(get("/v1/items")
                .param("category", "electronics")
                .header("X-API-Key", "bad-key"))
            .andExpect(status().isUnauthorized());
    }
    @Test
    void contextLoads() {
        // Placeholder so the test class is non-empty. Replace with real tests.
    }
}
