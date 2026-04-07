package com.economysim;

import java.util.List;
import java.util.Random;

public class EventManager {
    private Random random;
    private static final double EVENT_CHANCE = 0.3;

    public EventManager() {
        this.random = new Random();
    }

    public void triggerEvent(List<Market> markets, List<Item> items) {
        if (random.nextDouble() > EVENT_CHANCE) return;

        Item affectedItem = items.get(random.nextInt(items.size()));
        Market affectedMarket = markets.get(random.nextInt(markets.size()));
        boolean isBoom = random.nextBoolean();

        if (isBoom) {
            affectedMarket.applyEventMultiplier(affectedItem, 1.3);
            System.out.println("EVENT: high demand for " + affectedItem.getName()
                    + " in " + affectedMarket.getCity().getName() + "! Prices up 30%!");
        } else {
            affectedMarket.applyEventMultiplier(affectedItem, 0.7);
            System.out.println("EVENT: high demand for " + affectedItem.getName()
                    + " in " + affectedMarket.getCity().getName() + "! Prices down 30%!");
        }
    }
}
