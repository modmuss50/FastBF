package me.modmuss50.fastbf;

public class InjectingClassLoader extends ClassLoader {

    // Just here to make the protected method public
    public Class<?> defineClass(byte[] bytes, String name) {
        return defineClass(name, bytes, 0, bytes.length);
    }
}
