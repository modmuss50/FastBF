package me.modmuss50.fastbf;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class BytecodeCompiler {

    int loopMethods = 0;

    public byte[] compile(Operator operator, String classname) {
        // Start creating the class
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        classWriter.visit(V1_8, ACC_PUBLIC | ACC_FINAL | ACC_SUPER, classname, null, "java/lang/Object", null);
        classWriter.visitSource(classname +  ".java", null);

        {
            // Create the memory slide field
            FieldVisitor fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_STATIC, "memory", "[B", null, null);
            fieldVisitor.visitEnd();
        }

        {
            // Create the pointer field
            FieldVisitor fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_STATIC, "pointer", "I", null, null);
            fieldVisitor.visitEnd();
        }

        {
            // Create the scanner field
            FieldVisitor fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_STATIC, "scanner", "Ljava/util/Scanner;", null, null);
            fieldVisitor.visitEnd();
        }

        Map<String, Operator> loopMethods = new HashMap<>();
        {
            // Create the run method
            MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "run", "()V", null, null);
            methodVisitor.visitCode();

            {
                // init the fields
                methodVisitor.visitTypeInsn(NEW, "java/util/Scanner");
                methodVisitor.visitInsn(DUP);
                methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
                methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
                methodVisitor.visitFieldInsn(PUTSTATIC, classname, "scanner", "Ljava/util/Scanner;");
                methodVisitor.visitIntInsn(SIPUSH, 30000);
                methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
                methodVisitor.visitFieldInsn(PUTSTATIC, classname, "memory", "[B");
                methodVisitor.visitInsn(ICONST_0);
                methodVisitor.visitFieldInsn(PUTSTATIC, classname, "pointer", "I");
            }

            writeToMethod(methodVisitor, operator, classname, loopMethods);

            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }

        // Write out all of the loop methods recursively
        while (!loopMethods.isEmpty()) {
            Map<String, Operator> innerLoopMethods = new HashMap<>();
            for (Map.Entry<String, Operator> entry : loopMethods.entrySet()) {
                MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC, entry.getKey(), "()V", null, null);
                methodVisitor.visitCode();

                writeToMethod(methodVisitor, entry.getValue(), classname, innerLoopMethods);

                methodVisitor.visitInsn(RETURN);
                methodVisitor.visitMaxs(0, 0);
                methodVisitor.visitEnd();
            }

            loopMethods = innerLoopMethods;
        }

        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    private void writeToMethod(MethodVisitor methodVisitor, Operator operator, String className, Map<String, Operator> methods) {
        if (operator instanceof Operator.List) {
            for (Operator subOp : ((Operator.List) operator).getOperators()) {
                writeToMethod(methodVisitor, subOp, className, methods);
            }
        } else if (operator instanceof Operator.Pointer) {
            int amount = ((Operator.Pointer) operator).getAmount();

            // Add or subtract the the amount from the pointer field
            methodVisitor.visitFieldInsn(GETSTATIC, className, "pointer", "I");
            methodVisitor.visitIntInsn(BIPUSH, amount);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitFieldInsn(PUTSTATIC, className, "pointer", "I");
        } else if (operator instanceof Operator.Value) {
            int amount = ((Operator.Value) operator).getAmount();
            // Add or subtract from the memory tape at the pointer location
            methodVisitor.visitFieldInsn(GETSTATIC, className, "memory", "[B");
            methodVisitor.visitFieldInsn(GETSTATIC, className, "pointer", "I");
            methodVisitor.visitInsn(DUP2);
            methodVisitor.visitInsn(BALOAD);
            methodVisitor.visitIntInsn(BIPUSH, amount);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitInsn(I2B);
            methodVisitor.visitInsn(BASTORE);
        } else if (operator instanceof Operator.Print) {
            // Print out the current memory value casted to a char
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitFieldInsn(GETSTATIC, className, "memory", "[B");
            methodVisitor.visitFieldInsn(GETSTATIC, className, "pointer", "I");
            methodVisitor.visitInsn(BALOAD);
            methodVisitor.visitInsn(I2C);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(C)V", false);
        } else if (operator instanceof Operator.Input) {
            // Read a single char from the scanner and save to the current pointer location
            methodVisitor.visitFieldInsn(GETSTATIC, className, "memory", "[B");
            methodVisitor.visitFieldInsn(GETSTATIC, className, "pointer", "I");
            methodVisitor.visitFieldInsn(GETSTATIC, className, "scanner", "Ljava/util/Scanner;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "next", "()Ljava/lang/String;", false);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
            methodVisitor.visitInsn(I2B);
            methodVisitor.visitInsn(BASTORE);
        } else if (operator instanceof Operator.Loop) {
            // While loop
            Label outerLabel = new Label();
            methodVisitor.visitLabel(outerLabel);
            methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
            methodVisitor.visitFieldInsn(GETSTATIC, className, "memory", "[B");
            methodVisitor.visitFieldInsn(GETSTATIC, className, "pointer", "I");
            methodVisitor.visitInsn(BALOAD);
            Label innerLabel = new Label();
            methodVisitor.visitJumpInsn(IFEQ, innerLabel);

            String methodName = "loop_" + loopMethods;
            loopMethods ++;
            methods.put(methodName, ((Operator.Loop) operator).getContent());

            // Call the added method
            methodVisitor.visitMethodInsn(INVOKESTATIC, className, methodName, "()V", false);

            methodVisitor.visitJumpInsn(GOTO, outerLabel);
            methodVisitor.visitLabel(innerLabel);
            methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
