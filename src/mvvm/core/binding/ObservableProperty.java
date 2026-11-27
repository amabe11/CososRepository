package mvvm.core.binding;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Propiedad observable: la pieza que hace posible el data binding en MVVM.
 *
 * <p>El ViewModel cambia el valor con {@link #set} y la View, suscrita con
 * {@link #observe}, se actualiza sola. El ViewModel nunca conoce a la View.</p>
 *
 * <p>Si el nuevo valor es igual al actual no se notifica a nadie.</p>
 *
 * @param <T> tipo del valor
 */
public final class ObservableProperty<T> {

    private final List<Consumer<? super T>> observers = new CopyOnWriteArrayList<>();
    private volatile T value;

    public ObservableProperty(T initialValue) {
        this.value = initialValue;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        if (Objects.equals(value, newValue)) {
            return;
        }
        value = newValue;
        for (Consumer<? super T> observer : observers) {
            observer.accept(newValue);
        }
    }

    /**
     * Suscribe un observador y le entrega de inmediato el valor actual.
     *
     * @return acción que cancela la suscripción
     */
    public Runnable observe(Consumer<? super T> observer) {
        Objects.requireNonNull(observer, "observer");
        observers.add(observer);
        observer.accept(value);
        return () -> observers.remove(observer);
    }
}
