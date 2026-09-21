package com.shopsphere.repository;

import com.shopsphere.entity.RoadEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadEdgeRepository extends JpaRepository<RoadEdge, Long> {
    List<RoadEdge> findBySourceNode(String sourceNode);
}
