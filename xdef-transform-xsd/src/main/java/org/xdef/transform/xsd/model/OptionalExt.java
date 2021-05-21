package org.xdef.transform.xsd.model;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Extension of {@link Optional} class. Provides ifPresentOrElse() (similar to Java 9+)
 * @author smid
 * @since 2021-05-21
 */
public class OptionalExt<T> {

    private final Optional<T> optional;

    public OptionalExt(Optional<T> optional) {
        this.optional = optional;
    }

    public void ifPresentOrElse(final Consumer<? super T> consumer, final IfNotPresentFunction function) {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        } else {
            function.apply();
        }
    }

    public OptionalExt ifPresent(final Consumer<? super T> consumer) {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        }

        return this;
    }

    public void orElse(final IfNotPresentFunction function) {
        if (!optional.isPresent()) {
            function.apply();
        }
    }

    @FunctionalInterface
    public interface IfNotPresentFunction {

        void apply();

    }

}
