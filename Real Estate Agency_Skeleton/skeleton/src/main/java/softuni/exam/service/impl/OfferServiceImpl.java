package softuni.exam.service.impl;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.ImportOfferDTO;
import softuni.exam.models.dto.ImportOfferRootDTO;
import softuni.exam.models.entity.Agent;
import softuni.exam.models.entity.Apartment;
import softuni.exam.models.entity.ApartmentType;
import softuni.exam.models.entity.Offer;
import softuni.exam.repository.AgentRepository;
import softuni.exam.repository.ApartmentRepository;
import softuni.exam.repository.OfferRepository;
import softuni.exam.service.OfferService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OfferServiceImpl implements OfferService {
    private final static Path path = Path.of("src", "main", "resources", "files", "xml", "offers.xml");

    private final OfferRepository offerRepository;
    private final AgentRepository agentRepository;
    private final ApartmentRepository apartmentRepository;

    private final Unmarshaller unmarshaller;
    private final Validator validator;
    private final ModelMapper mapper;

    @Autowired
    public OfferServiceImpl(OfferRepository offerRepository, AgentRepository agentRepository, ApartmentRepository apartmentRepository) throws JAXBException {
        this.offerRepository = offerRepository;
        this.agentRepository = agentRepository;
        this.apartmentRepository = apartmentRepository;

        JAXBContext context = JAXBContext.newInstance(ImportOfferRootDTO.class);
        this.unmarshaller = context.createUnmarshaller();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.mapper = new ModelMapper();
        this.mapper.addConverter(new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(MappingContext<String, LocalDate> context) {
                return LocalDate.parse(context.getSource(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        });
    }


    @Override
    public boolean areImported() {
        return this.offerRepository.count() > 0;
    }

    @Override
    public String readOffersFileContent() throws IOException {
        return Files.readString(path);
    }

    @Override
    public String importOffers() throws IOException, JAXBException {
        ImportOfferRootDTO rootDTO = (ImportOfferRootDTO) this.unmarshaller
                .unmarshal(new FileReader(path.toAbsolutePath().toString()));
        return rootDTO
                .getOffers()
                .stream()
                .map(this::importOffer)
                .collect(Collectors.joining("\n"));
    }

    private String importOffer(ImportOfferDTO importOfferDTO) {
        Set<ConstraintViolation<ImportOfferDTO>> errors = this.validator.validate(importOfferDTO);
        if (!errors.isEmpty()) {
            return "Invalid offer";
        }
        Optional<Agent> optAgent = this.agentRepository.findByFirstName(importOfferDTO.getAgent().getName());
        if (optAgent.isEmpty()) {
            return "Invalid offer";
        }

        Offer offer = this.mapper.map(importOfferDTO, Offer.class);
        Optional<Apartment> apartment = this.apartmentRepository.findById(importOfferDTO.getApartment().getId());
        offer.setAgent(optAgent.get());
        offer.setApartment(apartment.get());
        this.offerRepository.save(offer);
        return String.format("Successfully imported offer %.2f",
                offer.getPrice());
    }

    @Override
    public String exportOffers() {
        List<Offer> offers = this.offerRepository.findByApartment_ApartmentTypeOrderByApartment_AreaDescPriceAsc(ApartmentType.three_rooms);
        return offers.stream().map(offer -> String.format(
                        "Agent %s %s with offer №%d:%n" +
                                "\t-Apartment area: %.2f%n" +
                                "\t--Town: %s%n" +
                                "\t---Price: %.2f$",
                        offer.getAgent().getFirstName(),
                        offer.getAgent().getLastName(),
                        offer.getId(),
                        offer.getApartment().getArea(),
                        offer.getApartment().getTown().getTownName(),
                        offer.getPrice()))
                .collect(Collectors.joining("\n"));
    }
}
