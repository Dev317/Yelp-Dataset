package com.cs.yelp_project.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessService {
    private BusinessRepository repository;

    @Autowired
    public BusinessService(BusinessRepository repository) {
        this.repository = repository;
    }

    public Iterable<Business> list() {
        return repository.findAll();
    }

    public Iterable<Business> save(List<Business> list) {
        return repository.saveAll(list);
    }

    public Business save(Business checkIn) {
        return repository.save(checkIn);
    }
}
