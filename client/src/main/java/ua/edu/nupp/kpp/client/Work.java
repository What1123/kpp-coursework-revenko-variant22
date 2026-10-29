package ua.edu.nupp.kpp.client;

import java.math.BigDecimal;

/** Клієнтська модель роботи сервісного замовлення. */
public class Work {

    private Long id;
    private long orderId;
    private String name;
    private double safetyScore;
    private double mobilityScore;
    private double laborHours;
    private BigDecimal cost;

    public Work() {
    }

    public Work(Long id, long orderId, String name, double safetyScore,
                double mobilityScore, double laborHours, BigDecimal cost) {
        this.id = id;
        this.orderId = orderId;
        this.name = name;
        this.safetyScore = safetyScore;
        this.mobilityScore = mobilityScore;
        this.laborHours = laborHours;
        this.cost = cost;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSafetyScore() {
        return safetyScore;
    }

    public void setSafetyScore(double safetyScore) {
        this.safetyScore = safetyScore;
    }

    public double getMobilityScore() {
        return mobilityScore;
    }

    public void setMobilityScore(double mobilityScore) {
        this.mobilityScore = mobilityScore;
    }

    public double getLaborHours() {
        return laborHours;
    }

    public void setLaborHours(double laborHours) {
        this.laborHours = laborHours;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "Work{id=" + id + ", orderId=" + orderId + ", name='" + name + "'}";
    }
}
