package com.cs.yelp_project.business;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends JpaRepository<Business,String> {
    List<Business> findByCity(String city);

    List<Business> findByCityAndCategories(String city, String categories);

    Optional<Business> findByName(String name);

    @Query("SELECT new com.cs.yelp_project.business.BusinessCheckInDTO(b.business_id, b.categories, b.state, c.total_checkin) FROM Business b INNER JOIN CheckIn c on b.business_id = c.business_id AND b.categories LIKE %:category%")
    List<BusinessCheckInDTO> findByCategoryType(String category);

}