package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;

/**
 * Servlet автомобілів. Прив'язка в web.xml: /cars.
 * <p>
 * GET  /cars                                   — список автомобілів;
 * POST /cars  action=add     clientId, vin, licensePlate, brand, model, productionYear, mileageKm;
 * POST /cars  action=update  id + ті самі параметри;
 * POST /cars  action=delete  id.
 * Після кожної POST-операції у відповідь повертається актуальний список автомобілів.
 */
public class CarServlet extends BaseJsonServlet {

    private final CarService service = new CarService();

    @Override
    protected Object handleGet(HttpServletRequest req) throws Exception {
        return service.getAll();
    }

    @Override
    protected Object handlePost(HttpServletRequest req) throws Exception {
        String action = action(req);
        switch (action) {
            case "add" -> service.add(readCar(req, null));
            case "update" -> service.update(readCar(req, requiredLong(req, "id")));
            case "delete" -> service.delete(requiredLong(req, "id"));
            default -> throw unknownAction(action);
        }
        return service.getAll();
    }

    /** Створює об'єкт Car із параметрів запиту; перевірку значень виконує CarService. */
    private static Car readCar(HttpServletRequest req, Long id) {
        return new Car(id,
                requiredLong(req, "clientId"),
                text(req, "vin"),
                text(req, "licensePlate"),
                text(req, "brand"),
                text(req, "model"),
                requiredInt(req, "productionYear"),
                requiredInt(req, "mileageKm"));
    }
}
