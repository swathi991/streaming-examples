package com.dsm.utils;

public class Test {
    public static void main(String[] args) {
        String string = "E Hello";
        solve(7, string);
        // System.out.println(solve(5, 2, "hello", "ll"));
    }

    public static void solve(int n, String str) {
        String result = "-1";
        if(n >= 1 && n <= 100)
            result = str.trim().replaceAll("[AaEeIiOoUu]", "");

        System.out.println(result);
    }

    public static String solve2(int n, int k, String str1, String str2) {
        if(n <= k)
            return "NO";
        else if(str1.contains(str2))
            return "YES";
        else
            return "NO";
    }
}
