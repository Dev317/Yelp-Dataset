package com.cs.yelp_project.controller;


import java.util.List;

import com.cs.yelp_project.business.Business;
import com.cs.yelp_project.business.BusinessService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/business")
public class BusinessController {
    private BusinessService service;

    @Autowired
    public BusinessController(BusinessService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public Iterable<Business> list() {
        return service.list();
    }

    // Shows DB based on city
    @GetMapping("/city")
    public List<Business> cityFilter(@RequestParam(name="name", required=false, defaultValue="Boulder") String city) {
        return service.filterByCity(city);
    }

    @GetMapping("/citycat")
    public List<Business> cityAndCategoriesFilter(@RequestParam(name="city", required=false, defaultValue="Boulder") String city, @RequestParam(name="categories", required=false, defaultValue="Boulder") String categories) {
        return service.filterByCityAndCategories(city, categories);
    }
}
