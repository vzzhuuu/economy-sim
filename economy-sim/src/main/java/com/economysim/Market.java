package com.economysim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Market {
    private City city;
    private Map<Item, Double> prices;
    private static final double SPECIALTY_DISCOUNT = 0.7;

    public Market(City city, List<Item> allItems) {
        this.city = city;
        this.prices = new HashMap<>();
        initializePrices(allItems);
    }

    private void initializePrices(List<Item> allItems) {
        for (Item item: allItems) {
            if (item == city.getSpecialtyItem()) {
                prices.put(item, item.getBasePrice() * SPECIALTY_DISCOUNT);
            } else {
                prices.put(item, item.getBasePrice());
            }
        }
    }

    public double getPrice(Item item) {
        return prices.getOrDefault(item, item.getBasePrice());
    }

    public City getCity() { return city; }

    // supply and demand logic
    public void onBuy(Item item) {
        double currentPrice = prices.get(item);
        prices.put(item, Math.round(currentPrice * 1.05 * 100.0) / 100.0);
    }

    public void onSell(Item item) {
        double currentPrice = prices.get(item);
        double basePrice = item.getBasePrice();
        double newPrice = Math.round(currentPrice * 0.95 * 100.0) / 100.0;
        prices.put(item, Math.max(newPrice, basePrice * 0.5));
    }

    public void applyEventMultiplier(Item item, double multiplier) {
        double currentPrice = prices.get(item);
        prices.put(item, Math.round(currentPrice * multiplier * 100.0) / 100.0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Market in ").append(city.getName()).append(":\n");
        for (Map.Entry<Item, Double> entry : prices.entrySet()) {
            sb.append("  ").append(entry.getKey().getName())
                    .append(": ").append(entry.getValue()).append("g\n");
        }
        return sb.toString();
    }
}
