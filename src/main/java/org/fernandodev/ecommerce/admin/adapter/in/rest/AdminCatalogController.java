package org.fernandodev.ecommerce.admin.adapter.in.rest;

import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminCatalogUseCases;
import org.fernandodev.ecommerce.admin.security.AdminRoles;
import org.fernandodev.ecommerce.admin.security.RequireGroup;
import org.fernandodev.ecommerce.admin.security.RequireRole;
import org.fernandodev.ecommerce.catalog.application.dto.BrandCommand;
import org.fernandodev.ecommerce.catalog.application.dto.CategoryCommand;
import org.fernandodev.ecommerce.catalog.application.dto.ProductCommand;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de administración del catálogo.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final AdminCatalogUseCases adminCatalog;

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@Valid @RequestBody ProductCommand command) {
        return adminCatalog.createProduct(command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductCommand command) {
        return adminCatalog.updateProduct(ProductId.of(id), command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/products/{id}/activate")
    public Product activateProduct(@PathVariable UUID id) {
        return adminCatalog.activateProduct(ProductId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/products/{id}/deactivate")
    public Product deactivateProduct(@PathVariable UUID id) {
        return adminCatalog.deactivateProduct(ProductId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable UUID id) {
        adminCatalog.deleteProduct(ProductId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(@Valid @RequestBody CategoryCommand command) {
        return adminCatalog.createCategory(command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PutMapping("/categories/{id}")
    public Category updateCategory(@PathVariable UUID id, @Valid @RequestBody CategoryCommand command) {
        return adminCatalog.updateCategory(CategoryId.of(id), command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/categories/{id}/activate")
    public Category activateCategory(@PathVariable UUID id) {
        return adminCatalog.activateCategory(CategoryId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/categories/{id}/deactivate")
    public Category deactivateCategory(@PathVariable UUID id) {
        return adminCatalog.deactivateCategory(CategoryId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable UUID id) {
        adminCatalog.deleteCategory(CategoryId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    public Brand createBrand(@Valid @RequestBody BrandCommand command) {
        return adminCatalog.createBrand(command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PutMapping("/brands/{id}")
    public Brand updateBrand(@PathVariable UUID id, @Valid @RequestBody BrandCommand command) {
        return adminCatalog.updateBrand(BrandId.of(id), command);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/brands/{id}/activate")
    public Brand activateBrand(@PathVariable UUID id) {
        return adminCatalog.activateBrand(BrandId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/brands/{id}/deactivate")
    public Brand deactivateBrand(@PathVariable UUID id) {
        return adminCatalog.deactivateBrand(BrandId.of(id));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @DeleteMapping("/brands/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBrand(@PathVariable UUID id) {
        adminCatalog.deleteBrand(BrandId.of(id));
    }
}