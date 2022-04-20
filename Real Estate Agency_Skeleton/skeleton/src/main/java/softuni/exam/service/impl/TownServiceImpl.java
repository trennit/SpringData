package softuni.exam.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.ImportTownDTO;
import softuni.exam.models.entity.Town;
import softuni.exam.repository.TownRepository;
import softuni.exam.service.TownService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TownServiceImpl implements TownService {
    private final static Path path = Path.of("src", "main", "resources", "files", "json", "towns.json");

    private final TownRepository townRepository;

    private final Validator validator;
    private final Gson gson;
    private final ModelMapper mapper;

    @Autowired
    public TownServiceImpl(TownRepository townRepository) {
        this.townRepository = townRepository;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.gson = new GsonBuilder().create();
        this.mapper = new ModelMapper();
    }

    @Override
    public boolean areImported() {
        return this.townRepository.count() > 0;
    }

    @Override
    public String readTownsFileContent() throws IOException {
        return Files.readString(path);
    }

    @Override
    public String importTowns() throws IOException {
        String json = this.readTownsFileContent();
        ImportTownDTO[] townDTOS = this.gson.fromJson(json, ImportTownDTO[].class);
        return Arrays.stream(townDTOS)
                .map(this::importTown)
                .collect(Collectors.joining("\n"));
    }

    private String importTown(ImportTownDTO importTownDTO) {
        Set<ConstraintViolation<ImportTownDTO>> errors = this.validator.validate(importTownDTO);
        if (!errors.isEmpty()) {
            return "Invalid town";
        }
        Town town = this.mapper.map(importTownDTO, Town.class);
        this.townRepository.save(town);
        return String.format("Successfully imported town %s - %d",
                town.getTownName(),
                town.getPopulation());
    }
}
