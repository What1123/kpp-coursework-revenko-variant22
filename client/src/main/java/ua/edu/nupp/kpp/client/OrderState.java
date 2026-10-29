package ua.edu.nupp.kpp.client;

/**
 * Клієнтська копія переліку станів сервісного замовлення.
 * Назви констант збігаються з серверними (CREATED, DIAGNOSED, REPAIR, COMPLETED),
 * тому JSON від сервера перетворюється на цей тип без додаткових правил.
 * Правила переходів між станами тут НЕ описані: їх перевіряє Service на сервері.
 */
public enum OrderState {

    CREATED("СТВОРЕНО"),
    DIAGNOSED("ДІАГНОСТОВАНО"),
    REPAIR("РЕМОНТ"),
    COMPLETED("ВИКОНАНО");

    private final String title;

    OrderState(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /** Українська назва показується в таблиці та у списку станів. */
    @Override
    public String toString() {
        return title;
    }
}
