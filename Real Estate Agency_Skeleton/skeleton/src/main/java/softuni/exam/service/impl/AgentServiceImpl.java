package softuni.exam.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.ImportAgentDTO;
import softuni.exam.models.entity.Agent;
import softuni.exam.models.entity.Town;
import softuni.exam.repository.AgentRepository;
import softuni.exam.repository.TownRepository;
import softuni.exam.service.AgentService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AgentServiceImpl implements AgentService {
    private final static Path path = Path.of("src", "main", "resources", "files", "json", "agents.json");

    private final AgentRepository agentRepository;
    private final TownRepository townRepository;

    private final Gson gson;
    private final Validator validator;
    private final ModelMapper mapper;

    @Autowired
    public AgentServiceImpl(AgentRepository agentRepository, TownRepository townRepository) {
        this.agentRepository = agentRepository;
        this.townRepository = townRepository;
        this.gson = new GsonBuilder().create();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.mapper = new ModelMapper();
    }

    @Override
    public boolean areImported() {
        return this.agentRepository.count() > 0;
    }

    @Override
    public String readAgentsFromFile() throws IOException {
        return Files.readString(path);
    }

    @Override
    public String importAgents() throws IOException {
        String json = this.readAgentsFromFile();
        ImportAgentDTO[] agentDTOS = this.gson.fromJson(json, ImportAgentDTO[].class);
        return Arrays.stream(agentDTOS)
                .map(this::importAgent)
                .collect(Collectors.joining("\n"));
    }

    private String importAgent(ImportAgentDTO importAgentDTO) {
        Set<ConstraintViolation<ImportAgentDTO>> errors = this.validator.validate(importAgentDTO);
        if (!errors.isEmpty()) {
            return "Invalid agent";
        }
        Optional<Agent> optAgent = this.agentRepository.findByFirstName(importAgentDTO.getFirstName());
        if (optAgent.isPresent()) {
            return "Invalid agent";
        }

        Agent agent = this.mapper.map(importAgentDTO, Agent.class);
        Optional<Town> town = this.townRepository.findByTownName(importAgentDTO.getTown());
        agent.setTown(town.get());
        this.agentRepository.save(agent);
        return String.format("Successfully imported agent - %s %s",
                agent.getFirstName(),
                agent.getLastName());
    }
}
