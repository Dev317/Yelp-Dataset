package com.cs.yelp_project.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.*;

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
