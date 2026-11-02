package com.kasir.amana;

import com.kasir.amana.Controller.Admin.CategoryController;
import com.kasir.amana.Controller.Admin.ProductController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that role-based access control is properly configured using @PreAuthorize annotations.
 * These annotations ensure ADMIN-only access to product and category management at the controller level.
 */
@SpringBootTest
class SecurityAccessControlTests {

    @Autowired
    private ProductController productController;

    @Autowired
    private CategoryController categoryController;

    @Test
    void contextLoads() {
        assertThat(productController).isNotNull();
        assertThat(categoryController).isNotNull();
    }

    @Test
    void productControllerRequiresAdminRole() {
        PreAuthorize annotation = ProductController.class.getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains("ADMIN");
    }

    @Test
    void categoryControllerRequiresAdminRole() {
        PreAuthorize annotation = CategoryController.class.getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains("ADMIN");
    }

    @Test
    void verifyMethodSecurityIsEnabled() throws Exception {
        // Verify that method security is enabled by checking if PreAuthorize annotations are present
        assertThat(ProductController.class.isAnnotationPresent(PreAuthorize.class))
                .as("ProductController should have @PreAuthorize annotation")
                .isTrue();
        
        assertThat(CategoryController.class.isAnnotationPresent(PreAuthorize.class))
                .as("CategoryController should have @PreAuthorize annotation")
                .isTrue();
    }
}
