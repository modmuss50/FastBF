package me.modmuss50.fastbf;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Throwable {
        // Take the current system time so we can time it roughly
        long start = System.currentTimeMillis();

        Operator operator;

        // Read the in the file passed from the first command line arg
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(args[0]))))) {
            operator = Parser.parse(reader);
        }

        BytecodeCompiler compiler = new BytecodeCompiler();
        byte[] classData = compiler.compile(operator, "Generated");

        // Use this to write out the generated class file, useful for debugging
        //Files.write(Paths.get("Generated.class"), classData);

        // Create a new classloader
        InjectingClassLoader classLoader = new InjectingClassLoader();

        // Pass the generated class bytecode to the class loader
        Class<?> clazz = classLoader.defineClass(classData, "Generated");

        // Use a MethodHandle to invoke the run method that was generated, the reflection api would work fine here as well
        MethodType methodType = MethodType.methodType(void.class);
        MethodHandle methodHandle = MethodHandles.publicLookup().findStatic(clazz, "run", methodType);
        methodHandle.invoke();

        System.out.println((System.currentTimeMillis() - start) + "ms");
    }

}
