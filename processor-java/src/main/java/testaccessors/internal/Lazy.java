package testaccessors.internal;

import java.util.function.Supplier;

final class Lazy<T> {
    private final Supplier<T> supplier;
    private volatile T value;

    Lazy(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    T getOrCompute() {
        final T result = value; // Just one volatile read
        return result == null ? maybeCompute(supplier) : result;
    }

    private synchronized T maybeCompute(Supplier<T> supplier) {
        if (value == null) {
            value = supplier.get();
            if (value == null) {
                throw new NullPointerException();
            }
        }
        return value;
    }
}
