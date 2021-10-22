package com.cs.yelp_project.kmeans;

import java.util.List;

import com.cs.yelp_project.business.Business;
import com.cs.yelp_project.business.BusinessRepository;

import org.springframework.beans.factory.annotation.Autowired;

public class Test {

    @Autowired
    BusinessRepository repository;

    List<Business> list = repository.findByCity("Seattle");
}
