package ua.edu.nupp.kpp.client;

/**
 * Помилка взаємодії із сервером.
 * Повідомлення вже зрозуміле користувачу: або текст поля "error" з JSON-відповіді сервера,
 * або опис проблеми зв'язку. Код стану HTTP дозволяє відрізнити помилку даних (400) від
 * недоступності сервера (0).
 */
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    /** HTTP-код відповіді сервера або 0, якщо відповіді не було (сервер недоступний). */
    public int getStatus() {
        return status;
    }
}
