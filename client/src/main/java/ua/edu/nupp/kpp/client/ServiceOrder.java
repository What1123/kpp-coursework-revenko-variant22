package ua.edu.nupp.kpp.client;

/**
 * Клієнтська модель сервісного замовлення.
 * Значення f1, f2, f3 та priorityIndex розраховує сервер (PriorityCalculator); клієнт лише
 * отримує їх і відображає. Дата створення приходить у JSON як рядок формату YYYY-MM-DD.
 */
public class ServiceOrder {

    private Long id;
    private long carId;
    private String description;
    private OrderState state;
    private String createdDate;
    private double f1;
    private double f2;
    private double f3;
    private double priorityIndex;

    public ServiceOrder() {
    }

    public ServiceOrder(Long id, long carId, String description, OrderState state,
                        String createdDate, double f1, double f2, double f3, double priorityIndex) {
        this.id = id;
        this.carId = carId;
        this.description = description;
        this.state = state;
        this.createdDate = createdDate;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.priorityIndex = priorityIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(long carId) {
        this.carId = carId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public double getPriorityIndex() {
        return priorityIndex;
    }

    public void setPriorityIndex(double priorityIndex) {
        this.priorityIndex = priorityIndex;
    }

    @Override
    public String toString() {
        return "ServiceOrder{id=" + id + ", carId=" + carId + ", state=" + state + "}";
    }
}
