package com.juanbenevento.wms.domain.model;

import com.juanbenevento.wms.domain.exception.DomainException;

public record LayoutContent(String rawJson) {
    public LayoutContent {
        if (rawJson == null || rawJson.isBlank()){
            throw new DomainException("El contenido del diseño no puede estar vacío.");
        }
    }

    public static LayoutContent empty(){
        return new LayoutContent("{}");
    }

    public boolean sameAs(LayoutContent other){
        return this.rawJson.equals(other.rawJson());
    }
}
