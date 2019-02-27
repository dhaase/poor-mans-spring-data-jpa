package eu.dirk.haase.jpa.data;

import eu.dirk.haase.jpa.data.internal.GenericReadOnlyRepository;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Main {

    public static void main(String... args) throws IllegalAccessException, InstantiationException {

        ReadOnlyRepository<Person, Long> repository1 = new CPersonReadOnlyRepository();

        Class<?> dynamicType1 = new ByteBuddy()
                .subclass(TypeDescription.Generic.Builder.parameterizedType(GenericReadOnlyRepository.class, Person.class, Long.class).build())
                .make()
                .load(Main.class.getClassLoader())
                .getLoaded();

        ReadOnlyRepository<Person, Long> repository2 = (ReadOnlyRepository<Person, Long>) dynamicType1.newInstance();

        Class<?> inferedEntityType;
        Class<?> inferedIDType;

        Type superclass = IPersonReadOnlyRepository.class.getGenericInterfaces()[0];
        if (superclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) superclass).getActualTypeArguments();
            if ((actualTypeArguments.length == 2) && (actualTypeArguments[0] instanceof Class)) {
                inferedEntityType = (Class<?>) actualTypeArguments[0];
                inferedIDType = (Class<?>) actualTypeArguments[1];
            } else {
                throw new IllegalStateException("Can not instantiate a generic type.");
            }
        } else {
            throw new IllegalStateException("Can not instantiate a raw type.");
        }

        final TypeDescription.Generic.Builder subclassBuilder = TypeDescription.Generic.Builder.parameterizedType(GenericReadOnlyRepository.class, inferedEntityType, inferedIDType);
        Class<?> dynamicType2 = new ByteBuddy()
                .subclass(subclassBuilder.build())
                .make()
                .load(Main.class.getClassLoader())
                .getLoaded();

        ReadOnlyRepository<Person, Long> repository3 = (ReadOnlyRepository<Person, Long>) dynamicType2.newInstance();

    }

}
