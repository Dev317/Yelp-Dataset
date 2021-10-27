package com.cs.yelp_project.checkin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheckInService {
    private CheckInRepository repository;

    @Autowired
    public CheckInService(CheckInRepository repository) {
        this.repository = repository;
    }

    public List<CheckIn> list() {
        return repository.findAll();
    }

    public List<CheckIn> save(List<CheckIn> list) {
        return repository.saveAll(list);
    }

    public CheckIn save(CheckIn checkIn) {
        return repository.save(checkIn);
    }
}
