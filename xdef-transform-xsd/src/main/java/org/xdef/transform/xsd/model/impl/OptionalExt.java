package org.xdef.transform.xsd.model.impl;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Extension of {@link Optional} class. Provides ifPresentOrElse() (similar to Java 9+)
 *
 * @author smid
 * @since 2021-05-21
 */
public class OptionalExt<T> {

    private final Optional<T> optional;

    private OptionalExt(Optional<T> optional) {
        this.optional = optional;
    }

    public static <T> OptionalExt<T> of(Optional<T> optional) {
        return new OptionalExt(optional);
    }

    public static <T> OptionalExt<T> ofNullable(T value) {
        return new OptionalExt(Optional.ofNullable(value));
    }

    public OptionalExt<T> ifPresent(final Consumer<? super T> consumer) {
        optional.ifPresent(consumer);
        return this;
    }

    public void orElse(final IfNotPresentFunction function) {
        if (!optional.isPresent()) {
            function.apply();
        }
    }

    public T orElseGet(Supplier<? extends T> other) {
        return optional.orElseGet(other);
    }

    @FunctionalInterface
    public interface IfNotPresentFunction {

        void apply();

    }

}
