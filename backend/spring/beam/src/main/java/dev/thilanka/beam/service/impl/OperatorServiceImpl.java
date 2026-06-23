package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.Mapper;
import dev.thilanka.beam.common.exception.DataNotChangedException;
import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.dto.OperatorDto;
import dev.thilanka.beam.entity.Operator;
import dev.thilanka.beam.repository.OperatorRepository;
import dev.thilanka.beam.service.OperatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorServiceImpl implements OperatorService {
    private final OperatorRepository operatorRepository;
    private final Mapper mapper;

    @Override
    public Operator createOperator(Operator operator) {
        return operatorRepository.save(operator);
    }

    @Override
    public OperatorDto createOperator(OperatorDto dto) {
        Operator operator = createOperator(dtoToOperator(dto));
        return mapper.toOperatorDto(operator);
    }

    @Override
    public List<OperatorDto> createOperators(List<OperatorDto> dtos) {

        List<OperatorDto> operatorDtos = new ArrayList<>();

        for (OperatorDto dto : dtos) {
            try {
                operatorDtos.add(createOperator(dto));
            } catch (Exception e) {
                log.warn("Error creating Operator name={}", dto.name(), e);
            }
        }

        return operatorDtos;
    }

    @Override
    public Operator updateOperator(Operator operator) {
        Operator existingOperator = findByName(operator.getName());

        if (existingOperator.getName().equals(operator.getName())) {
            throw new DataNotChangedException("Operator", "name", operator.getName());
        }
        existingOperator.setName(operator.getName());
        return operatorRepository.save(existingOperator);
    }

    @Override
    public OperatorDto updateOperator(OperatorDto dto) {
        Operator updated = updateOperator(dtoToOperator(dto));
        return mapper.toOperatorDto(updated);
    }

    @Override
    public List<OperatorDto> updateOperators(List<OperatorDto> dtos) {

        List<OperatorDto> updatedOperatorDtos = new ArrayList<>();

        for (OperatorDto dto : dtos) {
            try {
                updatedOperatorDtos.add(updateOperator(dto));
            } catch (Exception e) {
                log.warn("Error updating Operator name={}", dto.name(), e);
            }
        }

        return updatedOperatorDtos;
    }

    @Override
    public void deleteOperator(Long id) {

    }

    @Override
    public void deleteOperator(String operatorName) {
        Operator operator = findByName(operatorName);
//        operatorRepository.delete(operator);
    }

    @Override
    public Operator findByName(String name) {
        return operatorRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", "name", name));
    }

    @Override
    public OperatorDto findByOperatorName(String name) {
        Operator operator = findByName(name);
        return mapper.toOperatorDto(operator);
    }

    @Override
    public List<Operator> findAll() {
        return operatorRepository.findAll();
    }

    @Override
    public List<OperatorDto> getAll() {
        List<Operator> operators = findAll();
        return operators.stream().map(mapper::toOperatorDto).toList();
    }

    private Operator dtoToOperator(OperatorDto dto) {
        return Operator.builder()
                .name(dto.name())
                .build();
    }
}
