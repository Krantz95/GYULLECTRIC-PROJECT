package gyullectric.gyullectric.domain;

public enum ErrorCode {
    ERROR_101("101", "센서 통신 오류"),
    ERROR_102("102", "신호 전송 실패"),
    ERROR_103("103", "데이터 누락"),
    ERROR_110("110", "출력 저하"),
    ERROR_111("111", "출력 초과"),
    ERROR_201("201", "온도 센서 고장"),
    ERROR_202("202", "온도 초과"),
    EXCEPTION_ERROR("999", "알 수 없는 예외"),

    OUTPUT_LOW("301", "용접 출력 부족"),
    OUTPUT_HIGH("302", "용접 출력 과다"),
    TEMP_LOW("401", "도장 온도 부족"),
    TEMP_HIGH("402", "도장 온도 과다"),
    INSPECTION_FAIL("501", "조립 불량 검출");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
    public String getCode() { return code; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return code + " - " + description;
    }
}