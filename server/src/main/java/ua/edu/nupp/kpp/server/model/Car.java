package ua.edu.nupp.kpp.server.model;

/**
 * Автомобіль клієнта. Відповідає таблиці car.
 * Зв'язок: client.id → car.client_id (поле clientId).
 */
public class Car {

    private long id;
    private long clientId;
    private String vin;
    private String licensePlate;
    private String brand;
    private String model;
    private int productionYear;
    private int mileageKm;

    public Car() {
    }

    public Car(long id, long clientId, String vin, String licensePlate, String brand,
               String model, int productionYear, int mileageKm) {
        this.id = id;
        this.clientId = clientId;
        this.vin = vin;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.productionYear = productionYear;
        this.mileageKm = mileageKm;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(int productionYear) {
        this.productionYear = productionYear;
    }

    public int getMileageKm() {
        return mileageKm;
    }

    public void setMileageKm(int mileageKm) {
        this.mileageKm = mileageKm;
    }

    @Override
    public String toString() {
        return "Car{id=" + id + ", clientId=" + clientId + ", licensePlate='" + licensePlate
                + "', brand='" + brand + "', model='" + model + "'}";
    }
}
