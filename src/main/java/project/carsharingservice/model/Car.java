package project.carsharingservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "cars")
@Getter
@Setter
@SQLDelete(sql = "UPDATE cars SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "brand", nullable = false)
    private String brand;
    @Column(name = "model", nullable = false, unique = true)
    private String model;
    @Column(name = "type", columnDefinition = "varchar", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Type type;
    @Column(name = "inventory", nullable = false)
    private int inventory;
    @Column(name = "daily_fee", nullable = false)
    private BigDecimal dailyFee;
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public enum Type {
        SEDAN("sedan"),
        SUV("suv"),
        HATCHBACK("hatchback"),
        UNIVERSAL("universal");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public static Type findByValue(String value) {
            for (Type type : values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No enum found with value: " + value);
        }
    }
}
