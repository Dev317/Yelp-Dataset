package com.cs.yelp_project.business;

import com.cs.yelp_project.citystate.CityState;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue
    public Long id;

//    @ManyToMany
//    @JoinTable(name="category_businesses",
//            joinColumns=@JoinColumn(name="category_name"),
//            inverseJoinColumns=@JoinColumn(name="business_id"))
//    public List<Business> businesses;

    public String name;


    public Category() {}

    public Category(String categoryName) {
        this.name = categoryName;
    }
}
