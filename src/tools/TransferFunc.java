package tools;

public class TransferFunc {
    /* ------------------------------ Best one ----------------------------- */
    public static double shiftedSigmoid(double a) {
        double z = 10 * (a - 0.5);
        return 1 / (1 + Math.exp(-z));
    }

    public static double shiftedSigmoid2(double a) {
        double z = 10 * (a - 0.5);
        return 1 / (1 + Math.exp(-z));
    }

    /* ------------------------ U shaped decent ------------------------ */
    public static double Ushaped1(double a) {
        return Math.pow(Math.abs(a), 1.5);
    }

    public static double Ushaped2(double a) {
        return Math.pow(Math.abs(a), 2);
    }

    public static double Ushaped3(double a) {
        return Math.pow(Math.abs(a), 3);
    }

    /* ------------------------- Taper shaped decent ------------------------ */
    public static double Tshaped1(double a) {
        return Math.sqrt(Math.abs(a));
    }

    public static double Tshaped2(double a) {
        return Math.abs(a);
    }

    public static double Tshaped3(double a) {
        return Math.cbrt(Math.abs(a));
    }

    public static double Tshaped4(double a) {
        return Math.pow(Math.abs(a), 1.0 / 4.0);
    }

    /* ----------------------- S shaped generally bad ---------------------- */
    public static double Sshaped1(double a) {
        return 1 / (1 + Math.exp(-a * 2));
    }

    public static double Sshaped3(double a) {
        return 1 / (1 + Math.exp(-a / 2));
    }

    public static double Sshaped4(double a) {
        return 1 / (1 + Math.exp(-a / 3));

    }

    /* ------------------------- V shaped kinda bad ------------------------ */
    public static double Vshaped2(double a) {
        return Math.abs(Math.tanh(a));
    }

    public static double Vshaped3(double a) {
        return Math.abs(a / Math.sqrt(1 + Math.pow(a, 2)));
    }

    public static double Vshaped4(double a) {
        return Math.abs((2 / Math.PI) * Math.atan(Math.PI / 2 * a));
    }
}
