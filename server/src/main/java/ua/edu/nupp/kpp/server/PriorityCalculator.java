package ua.edu.nupp.kpp.server;

import java.util.List;

/**
 * Розрахунок факторів F1-F3 та інтегрального показника (індексу пріоритету) замовлення.
 * <p>
 * I = 0,40·F1 + 0,35·F2 + 0,25·F3, усі фактори нормовані до діапазону 0-100:
 * <ul>
 *   <li>F1 - вплив на безпеку: найбільша оцінка safetyScore серед робіт замовлення;</li>
 *   <li>F2 - вплив на мобільність: найбільша оцінка mobilityScore серед робіт замовлення;</li>
 *   <li>F3 - тривалість очікування: сумарна трудомісткість робіт у нормо-годинах,
 *       нормована до {@value #WAITING_NORM_HOURS} нормо-годин (100 і більше обмежується до 100).</li>
 * </ul>
 * Для замовлення без робіт усі фактори й показник дорівнюють 0.
 * Клас не є моделлю і не є DAO: він лише виконує обчислення для Service-рівня.
 */
public final class PriorityCalculator {

    /** Трудомісткість (нормо-годин), якій відповідає F3 = 100. */
    public static final double WAITING_NORM_HOURS = 40.0;

    private PriorityCalculator() {
    }

    /** F1 - найбільша оцінка впливу на безпеку серед робіт (0, якщо робіт немає). */
    public static double factor1(List<Work> works) {
        double max = 0.0;
        if (works != null) {
            for (Work work : works) {
                max = Math.max(max, work.getSafetyScore());
            }
        }
        check(max);
        return max;
    }

    /** F2 - найбільша оцінка впливу на мобільність серед робіт (0, якщо робіт немає). */
    public static double factor2(List<Work> works) {
        double max = 0.0;
        if (works != null) {
            for (Work work : works) {
                max = Math.max(max, work.getMobilityScore());
            }
        }
        check(max);
        return max;
    }

    /** F3 = min(100; сума нормо-годин / 40 · 100), округлене до двох знаків. */
    public static double factor3(List<Work> works) {
        double hours = 0.0;
        if (works != null) {
            for (Work work : works) {
                hours += work.getLaborHours();
            }
        }
        if (!(hours >= 0.0)) {
            throw new IllegalArgumentException("Некоректна трудомісткість робіт для F3");
        }
        return round2(Math.min(100.0, hours / WAITING_NORM_HOURS * 100.0));
    }

    /** I = 0,40·F1 + 0,35·F2 + 0,25·F3, результат округлюється до двох знаків після коми. */
    public static double calculate(double f1, double f2, double f3) {
        check(f1);
        check(f2);
        check(f3);
        return round2(0.40 * f1 + 0.35 * f2 + 0.25 * f3);
    }

    private static void check(double value) {
        if (!(value >= 0.0 && value <= 100.0)) {
            throw new IllegalArgumentException("Фактор має бути в діапазоні 0..100");
        }
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
