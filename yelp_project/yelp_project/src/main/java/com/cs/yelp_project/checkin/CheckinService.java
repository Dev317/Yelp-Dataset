package com.cs.yelp_project.checkin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheckinService {

    private CheckInRepository repository;

    @Autowired
    public CheckinService(CheckInRepository repository) {
        this.repository = repository;
    }

    public Iterable<CheckIn> list() {
        return repository.findAll();
    }

    public Iterable<CheckIn> save(List<CheckIn> list) {
        return repository.saveAll(list);
    }

    public CheckIn save(CheckIn checkIn) {
        return repository.save(checkIn);
    }
}
