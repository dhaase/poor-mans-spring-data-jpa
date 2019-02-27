package eu.dirk.haase.jpa.data;

import java.util.List;
import java.util.Optional;

public interface ReadOnlyRepository<T, ID> {

    List<T> findAll();

    Optional<T> findById(ID id);

    T getOne(ID id);

}
