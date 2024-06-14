package com.zalo.Spring_Zalo.Controller;

import com.zalo.Spring_Zalo.DTO.EventDto;
import com.zalo.Spring_Zalo.Entities.Company;
import com.zalo.Spring_Zalo.Entities.Event;
import com.zalo.Spring_Zalo.Entities.Product;
import com.zalo.Spring_Zalo.Entities.ProductEvent;
import com.zalo.Spring_Zalo.Repo.EventRepo;
import com.zalo.Spring_Zalo.Repo.ProductRepo;
import com.zalo.Spring_Zalo.Service.CompanyService;
import com.zalo.Spring_Zalo.Service.EventService;

import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@CrossOrigin("*")
public class EventController {

    @Autowired
    private EventService eventService;
    @Autowired
    private EventRepo eventRepo;

    @Autowired
    private CompanyService companyService;
    @Autowired
    private ProductRepo productRepo;

    @PostMapping("/")
    public ResponseEntity<Event> createEvent(@RequestBody Event event,
            @RequestParam(name = "companyId", required = false) Integer companyId) {
        Event e = eventService.createEvent(event, companyId);
        return new ResponseEntity<>(e, HttpStatus.CREATED);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEventInof(@PathVariable Integer eventId) {
        try {
            System.out.println(eventId);
            Optional<Event> e = eventRepo.findById(eventId);
            if (e == null) {
                throw new ResponseStatusException(404, "This event not found", null);
            }
            return new ResponseEntity<>(e.get(), HttpStatus.OK);
        } catch (Exception ex) {
            // Xử lý ngoại lệ ở đây nếu cần thiết
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex);
        }
    }

    // @PutMapping("/{eventId}")
    // public ResponseEntity<Event> updateEvent(@RequestBody Event
    // event,@PathVariable Integer eventId ,@RequestParam(name = "companyId",
    // required = false) Integer companyId){
    // Event e = eventService.updataEvent(event,eventId,companyId);
    // return new ResponseEntity<>(e, HttpStatus.OK);
    // }

    @GetMapping("/")
    public ResponseEntity<Page<Event>> getAllEventsWithPagination(@RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Page<Event> eventsPage = eventRepo.findAll(pageable);

        if (eventsPage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(eventsPage);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEventVisibility(@PathVariable("id") Long id,
            @RequestBody EventDto updatedEvent) {

        Optional<Event> eventOptional = eventRepo.findById(id.intValue());
        System.out.println(">>" + updatedEvent.isVisible());
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            // Update event visibility with the provided data
            event.setVisible(updatedEvent.isVisible());
            // Save the updated event to the database
            eventRepo.save(event);
            return new ResponseEntity<>(event, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
