package com.juanbenevento.wms.warehouse.domain.model;

public enum ZoneType {
    // --- ZONAS DE ALMACENAMIENTO (RACKS) ---
    DRY_STORAGE,    // Seco / General
    COLD_STORAGE,   // Refrigerado (0 a 5 grados)
    FROZEN_STORAGE, // Congelado (-18 grados)
    HAZMAT,         // Materiales Peligrosos

    // --- ZONAS OPERATIVAS (FLOOR / PLAYAS) ---
    RECEIVING_AREA, // Playa de Recepción
    PICKING_AREA,   // Zona de Armado / Staging
    DISPATCH_AREA,  // Playa de Despacho
    DOCK_DOOR,      // Puerta de Muelle
    YARD,           // Patio de Maniobras
    QUARANTINE      // Zona de Bloqueo/Cuarentena
}