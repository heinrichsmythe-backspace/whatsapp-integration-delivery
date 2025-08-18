package za.co.backspace.whatsappintegration.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import za.co.backspace.whatsappintegration.dtos.SampleRequestDTO;
import za.co.backspace.whatsappintegration.dtos.SampleResponseDTO;
import za.co.backspace.whatsappintegration.services.BasicService;

@RestController("api")
public class BasicRestController {

    @Autowired
    private BasicService basicService;

    @PostMapping("create")
    ResponseEntity<SampleResponseDTO> create(@RequestBody SampleRequestDTO body) {

        basicService.doSomething(body);

        return ResponseEntity.ok(SampleResponseDTO.builder()
                .success(true)
                .build());

    }

    @GetMapping("test2")
    ResponseEntity<SampleResponseDTO> test() {
        return ResponseEntity.ok(SampleResponseDTO.builder()
                .success(true)
                .build());

    }
}
