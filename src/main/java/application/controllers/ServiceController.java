package application.controllers;

import application.servicies.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping(path = "/api/service")
public class ServiceController {
    ServiceService serviceService;


    @Autowired
    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }


    // Безвозвратное удаление всей пользовательской информации из базы данных.
    @PostMapping(path = "/clear")
    public ResponseEntity clear() {
        serviceService.clear();
        return ResponseEntity.ok("Очистка базы успешно завершена");
    }


    // Получение инфомарции о базе данных.
    @GetMapping(path = "/status")
    public ResponseEntity status() {
        return ResponseEntity.ok(serviceService.status());
    }
}
