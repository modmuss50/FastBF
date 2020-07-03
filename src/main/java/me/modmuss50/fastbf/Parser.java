package me.modmuss50.fastbf;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

public class Parser {

    // Parse the brainfuck file to a tree
    public static Operator.List parse(BufferedReader reader) throws IOException {
        Deque<LinkedList<Operator>> operators = new LinkedList<>();
        operators.add(new LinkedList<>());

        int i;

        while ((i = reader.read()) != -1) {
            switch (i) {
                case '+':
                    operators.getLast().add(new Operator.Value(+1));
                    break;
                case '-':
                    operators.getLast().add(new Operator.Value(-1));
                    break;
                case '>':
                    operators.getLast().add(new Operator.Pointer(1));
                    break;
                case '<':
                    operators.getLast().add(new Operator.Pointer(-1));
                    break;
                case '[':
                    operators.add(new LinkedList<>());
                    break;
                case ']':
                    Operator.Loop loopOperator = new Operator.Loop(new Operator.List(optimise(operators.removeLast())));
                    operators.getLast().add(loopOperator);
                    break;
                case '.':
                    operators.getLast().add(new Operator.Print());
                    break;
                case ',':
                    operators.getLast().add(new Operator.Input());
                    break;
                default:
                    // Ignore anything else
            }
        }
        return new Operator.List(optimise(operators.removeLast()));
    }

    // Very basic and prob not so great optimisation.
    // Deduplicates subsequent operators into a single operator
    public static LinkedList<Operator> optimise(LinkedList<Operator> operators) {
        boolean fullyOptimised;
        do {
            Operator replacement = null;
            int i;
            for (i = 0; i < operators.size(); i++) {
                Operator a = operators.get(i);
                if (i + 1 != operators.size()) {
                    Operator b = operators.get(i + 1);

                    if (a instanceof Operator.Value && b instanceof Operator.Value) {
                        replacement = new Operator.Value(
                                ((Operator.Value) a).getAmount()
                                        +
                                        ((Operator.Value) b).getAmount()
                        );
                    } else if (a instanceof Operator.Pointer && b instanceof Operator.Pointer) {
                        replacement = new Operator.Pointer(
                                ((Operator.Pointer) a).getAmount()
                                        +
                                        ((Operator.Pointer) b).getAmount()
                        );
                    }
                }

                if (replacement != null) {
                    break;
                }
            }

            if (replacement != null) {
                operators.set(i, replacement);
                operators.remove(i + 1);
            }

            fullyOptimised = replacement == null;
        } while (!fullyOptimised);
        return operators;
    }

}
