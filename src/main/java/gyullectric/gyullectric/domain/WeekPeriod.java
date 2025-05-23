package gyullectric.gyullectric.domain;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public class WeekPeriod {

    private int weekNumber;
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;

    //기본 생성자
    public WeekPeriod() {}

    // 모든 필드를 받는 생성자
    public WeekPeriod(int weekNumber, int year, LocalDate startDate, LocalDate endDate) {
        this.weekNumber = weekNumber;
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
