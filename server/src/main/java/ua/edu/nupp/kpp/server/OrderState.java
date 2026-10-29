package ua.edu.nupp.kpp.server;

/**
 * Стани життєвого циклу сервісного замовлення:
 * СТВОРЕНО → ДІАГНОСТОВАНО → РЕМОНТ → ВИКОНАНО.
 * У базі даних зберігається назва константи (CREATED, DIAGNOSED, ...),
 * правила переходів між станами перевіряє Service-рівень.
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

    /** Українська назва стану для відображення користувачу. */
    public String getTitle() {
        return title;
    }
}
