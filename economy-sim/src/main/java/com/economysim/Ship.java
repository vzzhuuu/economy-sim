package com.economysim;

public class Ship {
    public enum Type {
        SLOOP, BRIGANTINE, GALLEON
    }

    private Type type;
    private int cargoCapacity;
    private int range;
    private double upgradeCost;

    public Ship() {
        this.type = Type.SLOOP;
        this.cargoCapacity = 4;
        this.range = 1;
        this.upgradeCost = 800.0;
    }

    public boolean upgrade(Player player) {
        if (type == Type.GALLEON) {
            System.out.println("Your ship is already at max level!");
            return false;
        }
        if (player.getGold() < upgradeCost) {
            System.out.println("Not enough gold to upgrade! Need " + upgradeCost + "g");
            return false;
        }
        player.setGold(player.getGold() - upgradeCost);
        if (type == Type.SLOOP) {
            type = Type.BRIGANTINE;
            cargoCapacity = 8;
            range = 2;
            upgradeCost = 1800;
            System.out.println("Upgraded to Brigantine! Capacity: 10, Range: 2");
        } else {
            type = Type.GALLEON;
            cargoCapacity = 15;
            range = 3;
            System.out.println("Upgraded to Galleon! Capacity = 15");
        }
        return true;
    }

    public int getCargoCapacity() { return cargoCapacity; };
    public int getRange() { return range; }
    public Type getType() { return type; }

    @Override
    public String toString() {
        return type.name().charAt(0) + type.name().substring(1).toLowerCase()
                + " | Capacity: " + cargoCapacity + " | Range: " + range;
    }
}
