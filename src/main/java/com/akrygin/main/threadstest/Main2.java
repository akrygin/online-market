package com.akrygin.main.threadstest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Future<Integer> future = new SquareCalculator().calculate(10);
        while(!future.isDone()) {
            System.out.println("Calculating...");
            Thread.sleep(1000);
        }
        Integer result = future.get();
        System.out.println(result);
    }
}