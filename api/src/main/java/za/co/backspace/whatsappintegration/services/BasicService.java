package za.co.backspace.whatsappintegration.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.backspace.whatsappintegration.dtos.SampleRequestDTO;
import za.co.backspace.whatsappintegration.persistence.entities.SomeEntity;
import za.co.backspace.whatsappintegration.persistence.repos.SomeEntityRepository;

import java.util.List;

@Service
public class BasicService {

    @Autowired
    SomeEntityRepository someEntityRepository;

    public void doSomething(SampleRequestDTO requestDTO) {
        System.out.println(requestDTO);

        SomeEntity someEntity = new SomeEntity();
        someEntity.setCol1("asdf");

        someEntity = someEntityRepository.save(someEntity);

        List<SomeEntity> entityList = someEntityRepository.findAll();
        System.out.println(entityList);
    }

}
