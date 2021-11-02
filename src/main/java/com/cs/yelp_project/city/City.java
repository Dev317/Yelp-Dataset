package com.cs.yelp_project.city;

import com.cs.yelp_project.business.Business;
import com.cs.yelp_project.business.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.Id;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Component
public class City {

    @Id
    private String name;

    private List<Business> businessList;
    private Set<Business> businessSet;


    private List<Category> categoryList;
    private Set<Category> categorySet;

    private Map<String, Integer> categoryFrequency;

    public City() {}

    public City(String name) {
        this.name = name;
    }
}
