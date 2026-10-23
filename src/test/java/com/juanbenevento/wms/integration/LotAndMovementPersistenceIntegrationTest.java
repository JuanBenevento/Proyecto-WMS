package com.juanbenevento.wms.integration;

import com.juanbenevento.wms.inventory.application.port.out.LotRepository;
import com.juanbenevento.wms.inventory.application.port.out.MovementRepository;
import com.juanbenevento.wms.inventory.domain.model.InventoryMovement;
import com.juanbenevento.wms.inventory.domain.model.Lot;
import com.juanbenevento.wms.inventory.domain.model.LotStatus;
import com.juanbenevento.wms.inventory.domain.model.TemperatureRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature real-persistence (B1) — S4: prueba que lotes y movimientos persisten
 * REALMENTE en PostgreSQL, no en un HashMap.
 *
 * Estrategia anti-engaño: además de usar los ports de la aplicación, cada dato
 * guardado se verifica con JDBC directo (JdbcTemplate) contra la tabla — un
 * adapter en memoria no puede superar estas aserciones aunque su API devuelva OK.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Lot and Movement real persistence (B1)")
class LotAndMovementPersistenceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("wms_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("S1: Flyway created inventory_lots and inventory_movements tables")
    void tablesExistInInformationSchema() {
        Long lots = countTable("inventory_lots");
        Long movements = countTable("inventory_movements");
        assertThat(lots)
                .as("tabla inventory_lots debe existir en information_schema (migración V3)")
                .isEqualTo(1L);
        assertThat(movements)
                .as("tabla inventory_movements debe existir en information_schema (migración V3)")
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("S3: a lot saved via port is visible through plain JDBC (not a map)")
    void lotSavedViaPortIsVisibleToPlainJdbc() {
        Lot lot = Lot.create("LOT-JDBC-001", "SKU-JDBC-001", "B-100", LocalDate.now(), "ORIGIN-A");

        Lot saved = lotRepository.save(lot);
        assertThat(saved.getLotNumber()).isEqualTo("LOT-JDBC-001");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT product_sku, status FROM inventory_lots WHERE lot_number = ?",
                "LOT-JDBC-001");
        assertThat(rows)
                .as("la fila debe ser visible por JDBC directo — un HashMap no persiste")
                .hasSize(1);
        assertThat(rows.get(0).get("product_sku")).isEqualTo("SKU-JDBC-001");
        assertThat(rows.get(0).get("status")).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("S2: full roundtrip preserves metadata JSON, temperature range and weights")
    void lotRoundtripPreservesAllFields() {
        Lot lot = Lot.create(
                "LOT-RT-001", "SKU-RT-001", "B-RT-1", LocalDate.of(2026, 1, 15), "FARM-RT",
                LocalDate.now().plusDays(45),
                TemperatureRange.coldStorage(),
                new BigDecimal("12.50"), new BigDecimal("13.00"),
                Map.of("calidad", "A", "variedad", "Roma"));

        lotRepository.save(lot);

        Optional<Lot> found = lotRepository.findByLotNumber("LOT-RT-001");
        assertThat(found).isPresent();
        Lot r = found.get();
        assertThat(r.getProductSku()).isEqualTo("SKU-RT-001");
        assertThat(r.getBatchNumber()).isEqualTo("B-RT-1");
        assertThat(r.getProductionDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(r.getOrigin()).isEqualTo("FARM-RT");
        assertThat(r.getExpiryDate()).isEqualTo(LocalDate.now().plusDays(45));
        assertThat(r.getStatus()).isEqualTo(LotStatus.ACTIVE);
        assertThat(r.getNetWeight()).isEqualByComparingTo("12.50");
        assertThat(r.getGrossWeight()).isEqualByComparingTo("13.00");
        assertThat(r.getTemperatureRange()).isNotNull();
        assertThat(r.getTemperatureRange().getMinTemperature())
                .isEqualByComparingTo(TemperatureRange.coldStorage().getMinTemperature());
        assertThat(r.getMetadata()).containsEntry("calidad", "A").containsEntry("variedad", "Roma");
    }

    @Test
    @DisplayName("S3: a movement saved via port is visible through plain JDBC")
    void movementSavedViaPortIsVisibleToPlainJdbc() {
        InventoryMovement movement = InventoryMovement.receive(
                "LOT-MOV-001", new BigDecimal("100"), "A-01-01", "operator1");

        movementRepository.save(movement);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT type, quantity, performed_by FROM inventory_movements WHERE movement_id = ?",
                movement.getMovementId());
        assertThat(rows)
                .as("movimiento visible por JDBC directo")
                .hasSize(1);
        assertThat(rows.get(0).get("type")).isEqualTo("RECEIPT");
        assertThat(new BigDecimal(rows.get(0).get("quantity").toString()))
                .isEqualByComparingTo("100");
        assertThat(rows.get(0).get("performed_by")).isEqualTo("operator1");
    }

    @Test
    @DisplayName("S3: findByLotNumber returns movements ordered by timestamp ascending")
    void movementsForLotAreOrderedByTimestamp() throws InterruptedException {
        InventoryMovement first = InventoryMovement.receive(
                "LOT-ORD-001", new BigDecimal("10"), "A-01", "op1");
        Thread.sleep(5); // asegura orden estricto de timestamps
        InventoryMovement second = InventoryMovement.transfer(
                "LOT-ORD-001", new BigDecimal("5"), "A-01", "A-02", "op1");

        movementRepository.save(first);
        movementRepository.save(second);

        List<InventoryMovement> found = movementRepository.findByLotNumber("LOT-ORD-001");
        assertThat(found).hasSize(2);
        assertThat(found.get(0).getMovementId()).isEqualTo(first.getMovementId());
        assertThat(found.get(1).getMovementId()).isEqualTo(second.getMovementId());
        assertThat(found.get(0).getTimestamp()).isBeforeOrEqualTo(found.get(1).getTimestamp());
    }

    @Test
    @DisplayName("S3: port queries work against persisted rows (expiring, active, counts)")
    void portQueriesWorkAgainstPersistedData() {
        Lot expiringSoon = Lot.create(
                "LOT-Q-EXP", "SKU-Q-001", "B-Q", LocalDate.now(), "O", LocalDate.now().plusDays(10),
                null, null, null, Map.of());
        Lot activeNoExpiry = Lot.create(
                "LOT-Q-ACT", "SKU-Q-002", "B-Q", LocalDate.now(), "O");
        lotRepository.save(expiringSoon);
        lotRepository.save(activeNoExpiry);

        assertThat(lotRepository.findExpiringWithin(30))
                .extracting(Lot::getLotNumber)
                .contains("LOT-Q-EXP");
        assertThat(lotRepository.findActiveLotsForAllocation("SKU-Q-002"))
                .extracting(Lot::getLotNumber)
                .contains("LOT-Q-ACT");
        assertThat(lotRepository.findByProductSku("SKU-Q-001"))
                .extracting(Lot::getLotNumber)
                .contains("LOT-Q-EXP");

        long before = lotRepository.countByStatus(LotStatus.ACTIVE);
        lotRepository.save(Lot.create("LOT-Q-CNT", "SKU-Q-003", "B-Q", LocalDate.now(), "O"));
        assertThat(lotRepository.countByStatus(LotStatus.ACTIVE)).isEqualTo(before + 1);
    }

    private Long countTable(String tableName) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = current_schema() AND table_name = ?",
                Long.class, tableName);
    }
}
