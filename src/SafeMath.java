public class SafeMath {
    static final int safeAdd(int left, int right) throws ArithmeticException {
        if (right > 0 && left > Integer.MAX_VALUE - right) {
            throw new ArithmeticException("Integer overflow");
        }
        if (right < 0 && left < Integer.MIN_VALUE - right) {
            throw new ArithmeticException("Integer overflow");
        }
        return left + right;
    }

    static final int safeSubtract(int left, int right) throws ArithmeticException {
        if (right > 0 && left < Integer.MIN_VALUE + right) {
            throw new ArithmeticException("Integer overflow");
        }
        if (right < 0 && left > Integer.MAX_VALUE + right) {
            throw new ArithmeticException("Integer overflow");
        }
        return left - right;
    }

    static final int safeMultiply(int left, int right) throws ArithmeticException {
        if (left == 0 || right == 0) {
            return 0;
        }
        if (left == Integer.MIN_VALUE && right == -1) {
            throw new ArithmeticException("Integer overflow");
        }
        if (left > Integer.MAX_VALUE / right || left < Integer.MIN_VALUE / right) {
            throw new ArithmeticException("Integer overflow");
        }
        return left * right;
    }
}
