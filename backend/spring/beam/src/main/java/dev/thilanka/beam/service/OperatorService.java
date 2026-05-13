package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.OperatorDto;
import dev.thilanka.beam.entity.Operator;

import java.util.List;

public interface OperatorService {

    Operator createOperator(Operator operator);

    OperatorDto createOperator(OperatorDto dto);

    List<OperatorDto> createOperators(List<OperatorDto> dtos);

    Operator updateOperator(Operator operator);

    OperatorDto updateOperator(OperatorDto dto);

    List<OperatorDto> updateOperators(List<OperatorDto> dtos);

    void deleteOperator(Long id);

    void  deleteOperator(String operatorName);

    Operator findByName(String name);

    OperatorDto findByOperatorName(String name);



}
