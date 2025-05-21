package gyullectric.gyullectric.domain;

public class ProductionAnalysis {
    private Long id;
    private String processName;
    private double avgProcessingTime;
    private PartName partName;
    private int currentInventory;
    private int predictedDemand;
    private int recommendedOrderAmount;
    private String dateUnit;

}
