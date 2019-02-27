package eu.dirk.haase.jpa.data.internal;

import eu.dirk.haase.jpa.data.ReadOnlyRepository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public abstract class GenericReadOnlyRepository<T, ID> implements ReadOnlyRepository<T, ID> {

    private final Class<T> inferedEntityType;
    private final Class<ID> inferedIDType;

    @SuppressWarnings("unchecked")
    public GenericReadOnlyRepository() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) superclass).getActualTypeArguments();
            if ((actualTypeArguments.length == 2) && (actualTypeArguments[0] instanceof Class)) {
                inferedEntityType = (Class<T>) actualTypeArguments[0];
                inferedIDType = (Class<ID>) actualTypeArguments[1];
                System.out.println(inferedEntityType);
                System.out.println(inferedIDType);
            } else {
                throw new IllegalStateException("Can not instantiate a generic type.");
            }
        } else {
            throw new IllegalStateException("Can not instantiate a raw type.");
        }
    }

    @Override
    public final List<T> findAll() {
        return null;
    }

    @Override
    public final Optional<T> findById(ID id) {
        return Optional.empty();
    }

    @Override
    public final T getOne(ID id) {
        return null;
    }
}
