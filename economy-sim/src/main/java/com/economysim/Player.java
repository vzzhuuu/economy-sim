package com.economysim;

import java.util.HashMap;
import java.util.Map;

public class Player {
    private double gold;
    private City currentCity;
    private Map<Item, Integer> inventory;

    public Player(double startingGold, City startingCity) {
        this.gold = startingGold;
        this.currentCity = startingCity;
        this.inventory = new HashMap<>();
    }

    public double getGold() { return gold; }
    public City getCurrentCity() { return currentCity; }
    public Map<Item, Integer> getInventory { return inventory; }

    public void setCurrentCity(City currentCity) { this.currentCity = currentCity; }
    public void setGold(double gold) { this.gold = gold; }

    @Override
    public String toString() {
        return "Player | Gold: " + gold + "g | City: " + currentCity.getName();
    }
}
