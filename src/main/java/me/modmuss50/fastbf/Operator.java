package me.modmuss50.fastbf;

import java.util.LinkedList;

public abstract class Operator {
    public static final class Pointer extends Operator {
        /**
         * Positive numbers move the pointer to the right x amount of blocks
         * Negative numbers move the pointer to the left x amount of blocks
         */
        private final int amount;

        public Pointer(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static final class Value extends Operator {
        /**
         * Positive numbers increase the value at the current memory location
         * Negative numbers increase the value at the current memory location
         */
        private final int amount;

        public Value(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static final class Loop extends Operator {
        private final List content;

        public Loop(List content) {
            this.content = content;
        }

        public List getContent() {
            return content;
        }
    }

    // Just an implementation detail, used to store a list of operators for example in a loop
    public static final class List extends Operator {
        private final LinkedList<Operator> operators;

        public List(LinkedList<Operator> operators) {
            this.operators = operators;
        }

        public LinkedList<Operator> getOperators() {
            return operators;
        }
    }

    public static final class Input extends Operator {
    }

    public static final class Print extends Operator {
    }
}
