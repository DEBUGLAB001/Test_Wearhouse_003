package com.shopsphere.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouses")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String city;

    @Column(nullable = false)
    private String locationNodeId;

    private Integer capacity;

    private boolean active = true;

    public Warehouse() {
    }

    public Warehouse(Long id, String code, String name, String city, String locationNodeId, Integer capacity) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.city = city;
        this.locationNodeId = locationNodeId;
        this.capacity = capacity;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocationNodeId() {
        return locationNodeId;
    }

    public void setLocationNodeId(String locationNodeId) {
        this.locationNodeId = locationNodeId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
