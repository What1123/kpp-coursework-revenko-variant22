package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;

/**
 * Servlet сервісних замовлень. Прив'язка в web.xml: /orders.
 * <p>
 * GET  /orders                                          — список замовлень зі станом, F1–F3 та індексом;
 * POST /orders  action=add         carId, description   — створити замовлення (стан СТВОРЕНО);
 * POST /orders  action=update      id, carId, description — змінити дані замовлення;
 * POST /orders  action=transition  id, state            — перевести замовлення в наступний стан;
 * POST /orders  action=recalculate id                   — перерахувати F1–F3 та індекс пріоритету;
 * POST /orders  action=delete      id                   — видалити замовлення.
 * Допустимість переходу стану й розрахунок індексу виконує ServiceOrderService, а не Servlet.
 */
public class ServiceOrderServlet extends BaseJsonServlet {

    private final ServiceOrderService service = new ServiceOrderService();

    @Override
    protected Object handleGet(HttpServletRequest req) throws Exception {
        return service.getAll();
    }

    @Override
    protected Object handlePost(HttpServletRequest req) throws Exception {
        String action = action(req);
        switch (action) {
            case "add" -> service.add(requiredLong(req, "carId"), text(req, "description"));
            case "update" -> service.update(requiredLong(req, "id"),
                    requiredLong(req, "carId"), text(req, "description"));
            case "transition" -> service.changeState(requiredLong(req, "id"),
                    requiredState(req, "state"));
            case "recalculate" -> service.recalculate(requiredLong(req, "id"));
            case "delete" -> service.delete(requiredLong(req, "id"));
            default -> throw unknownAction(action);
        }
        return service.getAll();
    }
}
