package com.github.paicoding.forum.web.javabetter.thread1;

import java.util.concurrent.atomic.AtomicReference;

class Account {
    static class Balance {
        final int money;
        final int points;

        Balance(int money, int points) {
            this.money = money;
            this.points = points;
        }
    }

    private AtomicReference<Balance> balance = new AtomicReference<>(new Balance(100, 10));

    public void update(int newMoney, int newPoints) {
        Balance oldBalance, newBalance;
        do {
            oldBalance = balance.get();
            newBalance = new Balance(newMoney, newPoints);
        } while (!balance.compareAndSet(oldBalance, newBalance));
    }
}
