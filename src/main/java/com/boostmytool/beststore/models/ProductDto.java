package com.boostmytool.beststore.models;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;


public class ProductDto {
    @NotEmpty(message = "The name is required")
    private String name;

    @NotEmpty(message = "The brand is required")
    private String brand;

    @NotEmpty(message = "The category is required")
    private String category;
    private double price;

    private String description;

    private String imageFileName;
}
