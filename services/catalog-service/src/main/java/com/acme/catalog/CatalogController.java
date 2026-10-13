package com.acme.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatalogController {
    @GetMapping("/api/catalog")
    public String getCatalog() {
        return "catalog-service";
    }
}
