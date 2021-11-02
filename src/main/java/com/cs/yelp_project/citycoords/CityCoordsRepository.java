package com.cs.yelp_project.citycoords;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityCoordsRepository extends JpaRepository<CityCoords, String> {
}
