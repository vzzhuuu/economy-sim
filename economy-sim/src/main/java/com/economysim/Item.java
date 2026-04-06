package com.economysim;

public class Item {
    private String name;
    private double basePrice;

    public Item(String name, double basePrice) {
        this.name = name;
        this.basePrice = basePrice;
    }

    public String getName() {
        return name;
    }

    public double getBasePrice() {
        return basePrice;
    }

    @Override
    public String toString() {
        return name + " (base price: " + basePrice + "g)";
    }
}
