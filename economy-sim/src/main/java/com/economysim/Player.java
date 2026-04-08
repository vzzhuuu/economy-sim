package com.economysim;

import java.util.HashMap;
import java.util.Map;

public class Player {
    private double gold;
    private City currentCity;
    private Map<Item, Integer> inventory;
    private int actionsRemaining;
    public static final int ACTIONS_PER_DAY = 5;

    public Player(double startingGold, City startingCity) {
        this.gold = startingGold;
        this.currentCity = startingCity;
        this.inventory = new HashMap<>();
    }

    // getters and setters
    public double getGold() { return gold; }
    public City getCurrentCity() { return currentCity; }
    public Map<Item, Integer> getInventory() { return inventory; }

    public void setCurrentCity(City currentCity) { this.currentCity = currentCity; }
    public void setGold(double gold) { this.gold = gold; }

    public boolean buy(Item item, Market market) {
        double price = market.getPrice(item);
        if (gold < price) {
            System.out.println("Not enough gold!");
            return false;
        }
        gold -= price;
        inventory.put(item, inventory.getOrDefault(item, 0) + 1);
        market.onBuy(item);
        return true;
    }

    public boolean sell(Item item, Market market) {
        if (inventory.getOrDefault(item, 0) <= 0) {
            System.out.println("You don't have that item!");
            return false;
        }
        double price = market.getPrice(item);
        gold += price;
        inventory.put(item, inventory.getOrDefault(item, 0) - 1);
        market.onSell(item);
        return true;
    }

    public boolean travel(City destination, double travelCost) {
        if (gold < travelCost) {
            System.out.println("Not enough gold to travel!");
            return false;
        }
        if (destination == currentCity) {
            System.out.println("You're already in " + destination.getName() + "!");
            return false;
        }
        gold -= travelCost;
        currentCity = destination;
        System.out.println("Travelled to " + destination.getName() + "!");
        return true;
    }

    // action handling methods
    public void resetActions() {
        actionsRemaining = ACTIONS_PER_DAY;
    }

    public boolean hasActions(int cost) {
        return actionsRemaining >= cost;
    }

    public void useActions(int cost) {
        actionsRemaining -= cost;
    }

    public int getActionsRemaining() {
        return actionsRemaining;
    }

    @Override
    public String toString() {
        return "Player | Gold: " + Math.round(gold * 100.0) / 100.0 + "g | City: " + currentCity.getName();
    }
}
