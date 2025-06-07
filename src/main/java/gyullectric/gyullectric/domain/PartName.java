package gyullectric.gyullectric.domain;

public enum PartName {
    BATTERY_PACK("배터리팩"),
    MOTOR("모터"),
    CONTROLLER("컨트롤러"),
    WHEEL("바퀴"),
    FRAME("프레임");

    private final String label;

    PartName(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // 🔽 name, label 모두 허용하는 매핑 메서드
    public static PartName fromString(String value) {
        for (PartName partName : PartName.values()) {
            if (partName.name().equalsIgnoreCase(value) ||
                    partName.label.equalsIgnoreCase(value) ||
                    partName.name().replace("_", "").equalsIgnoreCase(value.replace(" ", ""))) {
                return partName;
            }
        }
        throw new IllegalArgumentException("Invalid PartName: " + value);
    }
}
