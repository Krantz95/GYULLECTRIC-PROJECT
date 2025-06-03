package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.PartName;

public interface InventoryStockSummary {
    PartName getPartName();
    Integer getTotalQuantity();
}