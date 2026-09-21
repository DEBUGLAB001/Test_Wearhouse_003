package com.shopsphere.entity;

public enum OrderPriority {
    STANDARD(1),
    PRIORITY(2),
    EXPRESS(3);

    private final int level;

    OrderPriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
