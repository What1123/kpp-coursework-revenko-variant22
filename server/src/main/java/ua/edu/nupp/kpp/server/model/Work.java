package ua.edu.nupp.kpp.server.model;

import java.math.BigDecimal;

/**
 * Робота, що виконується в межах сервісного замовлення. Відповідає таблиці work.
 * Зв'язок: service_order.id → work.order_id (поле orderId).
 * <p>
 * safetyScore і mobilityScore (0-100) є основою для факторів F1 і F2 замовлення,
 * laborHours (нормо-години) - основою для фактора F3.
 */
public class Work {

    private long id;
    private long orderId;
    private String name;
    private double safetyScore;
    private double mobilityScore;
    private double laborHours;
    private BigDecimal cost;

    public Work() {
    }

    public Work(long id, long orderId, String name, double safetyScore,
                double mobilityScore, double laborHours, BigDecimal cost) {
        this.id = id;
        this.orderId = orderId;
        this.name = name;
        this.safetyScore = safetyScore;
        this.mobilityScore = mobilityScore;
        this.laborHours = laborHours;
        this.cost = cost;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
        return "Work{id=" + id + ", orderId=" + orderId + ", name='" + name
                + "', laborHours=" + laborHours + ", cost=" + cost + "}";
    }
}
