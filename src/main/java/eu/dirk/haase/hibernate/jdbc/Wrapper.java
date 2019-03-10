package eu.dirk.haase.hibernate.jdbc;

public interface Wrapper {
    <T> T unwrap(java.lang.Class<T> iface);
    boolean isWrapperFor(java.lang.Class<?> iface);

}
