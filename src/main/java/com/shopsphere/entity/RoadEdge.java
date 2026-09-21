package com.shopsphere.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "road_edges")
public class RoadEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceNode;

    @Column(nullable = false)
    private String targetNode;

    @Column(nullable = false)
    private Double distanceKm;

    public RoadEdge() {
    }

    public RoadEdge(Long id, String sourceNode, String targetNode, Double distanceKm) {
        this.id = id;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.distanceKm = distanceKm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(String sourceNode) {
        this.sourceNode = sourceNode;
    }

    public String getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(String targetNode) {
        this.targetNode = targetNode;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
