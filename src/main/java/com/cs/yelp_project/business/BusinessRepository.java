package com.cs.yelp_project.business;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends JpaRepository<Business,String> {
    List<Business> findByCity(String city);
}