# FastBF

This is a runtime brainfuck to JVM bytecode compiler. It is a lot faster than the other (limited amount) of brainfuck interpreters I have used.

I am not claiming this to be the fastest BF runtime, but it should be up there.

## How it works

1. Parse the brainfuck file to a simple tree of java objects
2. Apply some basic optimisations, such as combining operations into a single one
3. Using the ASM library produce a JVM 8 compatible classfile
4. Load this class into a custom class loader
5. Execute the generated method using method handles.

Here is a [decompiled java class](https://gist.github.com/modmuss50/2741dcfe4bb4c9b6acd0d44b9e8c3c03) of [Erik Bosman's brainfuck mandelbrot](https://github.com/erikdubbelboer/brainfuck-jit/blob/master/mandelbrot.bf).

Loop contents are put into their own methods due to java JIT optimisations skipping methods with many instructions, when ran without this it was no faster than an normal interpreter.

## Building

`./gradlew build` will output a compiled jar file into build/libs that can be ran using: 

`java -jar FastBF-1.0-SNAPSHOT.jar file.bf`
