package org.example;

import java.util.ArrayList;
import java.util.List;

public class CombinationGenerator {

    public static void main(String[] args) {
        String dictionary = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = 3; // 输入你想要的位数

        List<String> combinations = generateCombinations(dictionary, length);

        System.out.println("Generated Combinations:");
        for (String combination : combinations) {
            System.out.println(combination);
        }
    }

    private static List<String> generateCombinations(String dictionary, int length) {
        List<String> combinations = new ArrayList<>();
        int[] indices = new int[length];

        while (true) {
            StringBuilder currentCombination = new StringBuilder();
            for (int index : indices) {
                currentCombination.append(dictionary.charAt(index));
            }
            combinations.add(currentCombination.toString());

            // Increment indices
            int i = length - 1;
            while (i >= 0 && indices[i] == dictionary.length() - 1) {
                indices[i] = 0;
                i--;
            }
            if (i < 0) {
                break; // All combinations generated
            }
            indices[i]++;
        }

        return combinations;
    }
}
