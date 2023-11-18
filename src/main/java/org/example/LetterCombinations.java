package org.example;

public class LetterCombinations {
    public static void main(String[] args) {
        printCombinations(4); // 输入数字4，生成AAAA到ZZZZ
      //  printCombinations(5); // 输入数字5，生成AAAAA到ZZZZZ
        // 可以根据需要继续测试其他数字

    }

    public static void printCombinations(int num) {
        int totalCombinations = (int) Math.pow(26, num);

        for (int i = 0; i < totalCombinations; i++) {
            String combination = numberToLetters(i, num);
            System.out.println(i + ":" + combination);
            if("YYYY".equals(combination)){

                System.out.printf("");
            }
        }
    }

    private static String numberToLetters(int num, int length) {
        StringBuilder result = new StringBuilder();

        while (num >= 0 && length > 0) {
            result.insert(0, (char) ('A' + num % 26));
            num = num / 26 - 1;
            length--;
        }

        // Padding with 'A' for remaining length
        while (length > 0) {
            result.insert(0, 'A');
            length--;
        }

        return result.toString();
    }
}
