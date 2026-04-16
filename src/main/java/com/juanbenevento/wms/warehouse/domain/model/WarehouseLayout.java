package com.juanbenevento.wms.warehouse.domain.model;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class WarehouseLayout {
    private final String id;
    private final String tenantId;
    private LayoutContent content;
    private Integer version;
    private LocalDateTime updatedAt;

    public WarehouseLayout(String id, String tenantId, LayoutContent content, Integer version, LocalDateTime updatedAt){
        this.id = Objects.requireNonNull(id);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.content = Objects.requireNonNull(content);
        this.version = version;
        this.updatedAt = updatedAt;
    }

    public static  WarehouseLayout createEmpty(String tenantId){
        return new WarehouseLayout(
                UUID.randomUUID().toString(),
                tenantId,
                LayoutContent.empty(),
                1,
                LocalDateTime.now()
        );
    }

    public void updateDesign(LayoutContent newContent){
        if (this.content.sameAs(newContent)){
            return;
        }
        this.content = newContent;
        this.version++;
        this.updatedAt = LocalDateTime.now();
    }
}
