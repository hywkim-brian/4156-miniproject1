package com.taxapi.model;

/**
 * A request to update an item's price.
 */
public final class UpdatePriceRequest {

    /** new base price. */
    private Double basePrice;

    /** Creates an empty price update request. */
    public UpdatePriceRequest() {
    }

    /**
     * Gets the new base price.
     *
     * @return the new base price
     */
    public Double getBasePrice() {
        return basePrice;
    }

    /**
     * Sets the new base price.
     *
     * @param basePrice the new base price
     */
    public void setBasePrice(final Double basePrice) {
        this.basePrice = basePrice;
    }
}
