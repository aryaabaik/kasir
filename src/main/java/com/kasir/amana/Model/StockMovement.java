    package com.kasir.amana.Model;

    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import java.time.LocalDateTime;

    @Getter
    @Setter
    @NoArgsConstructor
    @Entity
    @Table(name = "stock_movements")
    public class StockMovement {
        @Id 
        @GeneratedValue(strategy =  GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "varian_id", nullable = false)
        private ProductVariant variant;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Column(nullable = false, length = 30)
        private String movementType;

        @Column(nullable = false)
        private Integer quantityChange;

        @Column(nullable = false)
        private Integer stockBefore;

        @Column(nullable = false)
        private Integer stockAfter;

        @Column(length = 255)
        private String notes;

        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();
    }
