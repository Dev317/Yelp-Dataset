package com.cs.yelp_project.business;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
