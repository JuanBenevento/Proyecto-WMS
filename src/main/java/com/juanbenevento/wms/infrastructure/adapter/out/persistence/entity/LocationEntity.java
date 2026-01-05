package com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity;

import com.juanbenevento.wms.domain.model.ZoneType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationEntity {

    @Id
    @Column(name = "location_code")
    private String locationCode;

    @Enumerated(EnumType.STRING)
    private ZoneType zoneType;

    private Double maxWeight;
    private Double maxVolume;

    private Double currentWeight;
    private Double currentVolume;

    @Version
    private Long version;
}