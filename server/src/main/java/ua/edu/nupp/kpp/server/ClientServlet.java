package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;

/**
 * Servlet клієнтів (власників автомобілів). Прив'язка в web.xml: /clients.
 * <p>
 * GET  /clients                                        — список клієнтів;
 * POST /clients  action=add     fullName, phone, email — додати клієнта;
 * POST /clients  action=update  id, fullName, phone, email — змінити клієнта;
 * POST /clients  action=delete  id                     — видалити клієнта.
 * Після кожної POST-операції у відповідь повертається актуальний список клієнтів.
 */
public class ClientServlet extends BaseJsonServlet {

    private final ClientService service = new ClientService();

    @Override
    protected Object handleGet(HttpServletRequest req) throws Exception {
        return service.getAll();
    }

    @Override
    protected Object handlePost(HttpServletRequest req) throws Exception {
        String action = action(req);
        switch (action) {
            case "add" -> service.add(readClient(req, null));
            case "update" -> service.update(readClient(req, requiredLong(req, "id")));
            case "delete" -> service.delete(requiredLong(req, "id"));
            default -> throw unknownAction(action);
        }
        return service.getAll();
    }

    /** Створює об'єкт Client із параметрів запиту; перевірку значень виконує ClientService. */
    private static Client readClient(HttpServletRequest req, Long id) {
        return new Client(id, text(req, "fullName"), text(req, "phone"), text(req, "email"));
    }
}
