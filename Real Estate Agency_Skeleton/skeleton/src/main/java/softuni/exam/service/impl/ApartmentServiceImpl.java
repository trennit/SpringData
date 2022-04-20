package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.ImportApartmentDTO;
import softuni.exam.models.dto.ImportApartmentRootDTO;
import softuni.exam.models.entity.Apartment;
import softuni.exam.models.entity.Town;
import softuni.exam.repository.ApartmentRepository;
import softuni.exam.repository.TownRepository;
import softuni.exam.service.ApartmentService;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApartmentServiceImpl implements ApartmentService {
    private final static Path path = Path.of("src", "main", "resources", "files", "xml", "apartments.xml");

    private final ApartmentRepository apartmentRepository;
    private final TownRepository townRepository;

    private final Unmarshaller unmarshaller;
    private final ModelMapper mapper;

    @Autowired
    public ApartmentServiceImpl(ApartmentRepository apartmentRepository, TownRepository townRepository) throws JAXBException {
        this.apartmentRepository = apartmentRepository;
        this.townRepository = townRepository;

        JAXBContext context = JAXBContext.newInstance(ImportApartmentRootDTO.class);
        this.unmarshaller = context.createUnmarshaller();
        this.mapper = new ModelMapper();
    }

    @Override
    public boolean areImported() {
        return this.apartmentRepository.count() > 0;
    }

    @Override
    public String readApartmentsFromFile() throws IOException {
        return Files.readString(path);
    }

    @Override
    public String importApartments() throws IOException, JAXBException {
        ImportApartmentRootDTO rootDTO = (ImportApartmentRootDTO) this.unmarshaller
                .unmarshal(new FileReader(path.toAbsolutePath().toString()));
        return rootDTO
                .getApartments()
                .stream()
                .map(this::importApartment)
                .collect(Collectors.joining("\n"));
    }

    private String importApartment(ImportApartmentDTO importApartmentDTO) {
        if (!importApartmentDTO.isValid()) {
            return "Invalid apartment";
        }
        Optional<Apartment> optApartment = this.apartmentRepository
                .findByTown_TownNameAndArea(importApartmentDTO.getTown(), importApartmentDTO.getArea());

        if (optApartment.isPresent()) {
            return "Invalid apartment";
        }
        Apartment apartment = this.mapper.map(importApartmentDTO, Apartment.class);
        Optional<Town> town = this.townRepository.findByTownName(importApartmentDTO.getTown());
        apartment.setTown(town.get());
        this.apartmentRepository.save(apartment);
        return String.format("Successfully imported apartment %s - %.2f",
                apartment.getApartmentType(),
                apartment.getArea());
    }
}
