package com.taxapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Tax API.
 */
@SpringBootApplication
public final class TaxApiApplication {

    /** Private constructor to prevent instantiation. */
    private TaxApiApplicaton() {
    }

    /**
     * Application entry point.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(
            TaxApiApplication.class, args
        )
    }
}
