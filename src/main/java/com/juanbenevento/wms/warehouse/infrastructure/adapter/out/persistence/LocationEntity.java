package com.juanbenevento.wms.warehouse.infrastructure.adapter.out.persistence;

import com.juanbenevento.wms.warehouse.domain.model.ZoneType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "locations", indexes = {
        @Index(name = "idx_loc_aisle", columnList = "aisle"),
        @Index(name = "idx_loc_zone", columnList = "zoneType")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationEntity {
    @Id
    @Column(name = "location_code", nullable = false, unique = true)
    private String locationCode;
    private String aisle;
    private String column_name;
    private String level_name;
    @Enumerated(EnumType.STRING)
    private ZoneType zoneType;
    private BigDecimal maxWeight;
    private BigDecimal maxVolume;
    private BigDecimal currentWeight;
    private BigDecimal currentVolume;
    @Version
    private Long version;
}