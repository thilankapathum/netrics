package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.CellNameDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CellNameService {

    List<CellNameDto> searchCell(String cellName);

    CompletableFuture<Integer> reloadCells();
}
