package com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity;

import com.juanbenevento.wms.domain.model.ZoneType;
import jakarta.persistence.*;
import lombok.*;

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
    private Double maxWeight;
    private Double maxVolume;
    private Double currentWeight;
    private Double currentVolume;
    @Version
    private Long version;
}