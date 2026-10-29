package ua.edu.nupp.kpp.server.model;

import java.time.LocalDate;

/**
 * Сервісне замовлення - об'єкт чотиристанного життєвого циклу.
 * Відповідає таблиці service_order.
 * Зв'язок: car.id → service_order.car_id (поле carId).
 * <p>
 * Поля f1, f2, f3 та priorityIndex розраховуються на сервері й мають діапазон 0-100:
 * F1 - вплив на безпеку, F2 - вплив на мобільність, F3 - тривалість очікування,
 * priorityIndex = 0,40·F1 + 0,35·F2 + 0,25·F3.
 */
public class ServiceOrder {

    private long id;
    private long carId;
    private String description;
    private OrderState state = OrderState.CREATED;
    private LocalDate createdDate;
    private double f1;
    private double f2;
    private double f3;
    private double priorityIndex;

    public ServiceOrder() {
    }

    public ServiceOrder(long id, long carId, String description, OrderState state,
                        LocalDate createdDate, double f1, double f2, double f3,
                        double priorityIndex) {
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
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
        return "ServiceOrder{id=" + id + ", carId=" + carId + ", state=" + state
                + ", createdDate=" + createdDate + ", priorityIndex=" + priorityIndex + "}";
    }
}
