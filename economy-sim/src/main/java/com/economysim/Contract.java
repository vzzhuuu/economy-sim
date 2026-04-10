package com.economysim;

public class Contract {
    private City targetCity;
    private Item requiredItem;
    private int requiredQuantity;
    private double reward;
    private int deadline;
    private boolean completed;

    public Contract(City targetCity, Item requireedItem, int requiredQuantity, double reward, int deadline) {
        this.targetCity = targetCity;
        this.requiredItem = requireedItem;
        this.requiredQuantity = requiredQuantity;
        this.reward = reward;
        this.deadline = deadline;
        this.completed = false;
    }

    public boolean canComplete(Player player, City currrentCity) {
        return currrentCity == targetCity
                && player.getInventory().getOrDefault(requiredItem, 0) >= requiredQuantity
                && !completed;
    }

    public void complete(Player player) {
        player.getInventory().put(requiredItem, player.getInventory().get(requiredItem) - requiredQuantity);
        player.setGold(player.getGold() + reward);
        completed = true;
        System.out.println("Contract completed! Earned " + reward + "g bonus!");
    }

    public void decrementDeadline() {
        deadline--;
    }

    public boolean isExpired() { return deadline <= 0 && !completed; }
    public boolean isCompleted() { return completed; }
    public City getTargetCity() { return targetCity; }
    public Item getRequiredItem() { return requiredItem; }
    public int getRequiredQuantity() { return requiredQuantity; }
    public double getReward() { return reward; }
    public int getDeadline() { return deadline; }

    @Override
    public String toString() {
        return "Deliver " + requiredQuantity + "x " + requiredItem.getName()
                + " to " + targetCity.getName()
                + " | Reward: " + reward + "g"
                + " | Deadline: " + deadline + " days";
    }
}
