package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;

/**
 * Servlet робіт сервісних замовлень. Прив'язка в web.xml: /works.
 * <p>
 * GET  /works                  — список усіх робіт;
 * GET  /works?orderId=N        — роботи одного замовлення;
 * POST /works  action=add      orderId, name, safetyScore, mobilityScore, laborHours, cost;
 * POST /works  action=update   id + ті самі параметри;
 * POST /works  action=delete   id.
 * Після додавання, зміни або видалення роботи WorkService перераховує F1–F3 та індекс
 * пріоритету замовлення. Після POST-операції повертається актуальний список усіх робіт.
 */
public class WorkServlet extends BaseJsonServlet {

    private final WorkService service = new WorkService();

    @Override
    protected Object handleGet(HttpServletRequest req) throws Exception {
        if (text(req, "orderId") != null) {
            return service.getByOrderId(requiredLong(req, "orderId"));
        }
        return service.getAll();
    }

    @Override
    protected Object handlePost(HttpServletRequest req) throws Exception {
        String action = action(req);
        switch (action) {
            case "add" -> service.add(readWork(req, null));
            case "update" -> service.update(readWork(req, requiredLong(req, "id")));
            case "delete" -> service.delete(requiredLong(req, "id"));
            default -> throw unknownAction(action);
        }
        return service.getAll();
    }

    /** Створює об'єкт Work із параметрів запиту; перевірку значень виконує WorkService. */
    private static Work readWork(HttpServletRequest req, Long id) {
        return new Work(id,
                requiredLong(req, "orderId"),
                text(req, "name"),
                requiredDouble(req, "safetyScore"),
                requiredDouble(req, "mobilityScore"),
                requiredDouble(req, "laborHours"),
                requiredDecimal(req, "cost"));
    }
}
