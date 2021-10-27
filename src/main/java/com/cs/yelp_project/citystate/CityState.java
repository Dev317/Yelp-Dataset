package com.cs.yelp_project.citystate;

import com.cs.yelp_project.business.Business;
import com.cs.yelp_project.business.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Component
public class CityState {

    @Id
    private String name;

    private List<Business> businessList;

    private List<Category> categoryList;

    private Map<String, Integer> categoryFrequency;

    public CityState() {}

    public CityState(String name) {
        this.name = name;
    }
}
