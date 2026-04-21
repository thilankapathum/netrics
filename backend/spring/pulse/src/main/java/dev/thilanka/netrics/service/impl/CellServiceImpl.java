package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.enums.CsvImportStatus;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.CellRepository;
import dev.thilanka.netrics.service.*;
import dev.thilanka.netrics.util.DataTypeUtilService;
import dev.thilanka.netrics.util.GeoUtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CellServiceImpl implements CellService {
    private final CellRepository cellRepository;
    private final RatService ratService;
    private final SiteService siteService;
    private final BandService bandService;
    private final Mapper mapper;
    private final GranularityService granularityService;
    private final KpiDayService kpiDayService;
    private final DateService dateService;
    private final CarrierService carrierService;
    private final SectorService sectorService;
    private final GeoUtilService geoUtilService;
    private final DataTypeUtilService dataTypeUtilService;

    Integer cellCountWithMissingInfo = 0;
    List<CellDto> allCells = new ArrayList<>();

    @Override
    public Cell createCell(Cell cell) {
        return cellRepository.save(cell);
    }

    @Override
    public CellDto createCell(CellDto dto) {
        Cell savedCell = createCell(dtoToCell(dto));
        return mapper.cellToDto(savedCell);
    }

    private Cell dtoToCell(CellDto dto) {
        Cell.CellBuilder cell = Cell.builder().cellName(dto.cellName());

        if (dto.nodeName() != null) {
            cell.nodeName(dto.nodeName());
        }

        if (dto.ratName() != null) {
            Rat rat = ratService.findRatByName(dto.ratName());
            cell.rat(rat);
        }

        if (dto.siteCode() != null) {
            Site site = siteService.findBySiteCode(dto.siteCode());
            cell.site(site);
        }

        if (dto.bandName() != null) {
            Band band = bandService.findByName(dto.bandName());
            cell.band(band);
        }

        if (dto.azimuth() != null) {
            cell.azimuth(dto.azimuth());
        }

        if (dto.beamwidth() != null) {
            cell.beamwidth(dto.beamwidth());
        }

        if (dto.isMultiBeam()) {
            cell.isMultiBeam(true);
        }

        if (dto.carrierName() != null) {
            Carrier carrier = carrierService.findByName(dto.carrierName());
            cell.carrier(carrier);
        }

        if (dto.sectorName() != null) {
            Sector sector = sectorService.findBySectorName(dto.sectorName());
            cell.sector(sector);
        }

        return cell.build();
    }

    @Override
    public List<CellDto> createCells(List<CellDto> dtos) {

        List<CellDto> savedDtos = new ArrayList<>();
        int duplicateCells = 0;
        int failedCells = 0;

        for (CellDto dto : dtos) {
            Optional<Cell> cell = cellRepository.findByCellName(dto.cellName());
            if (cell.isPresent()) {
                duplicateCells++;
            } else {

                try {
                    CellDto savedDto = createCell(dto);
                    savedDtos.add(savedDto);
                } catch (Exception e) {
                    failedCells++;
                    log.warn("failed saving cell. {}", e.getMessage());
                }
            }
        }

        log.info("Saved {}/{} cells. ", savedDtos.size(), dtos.size());
        if (duplicateCells > 0) log.info("Duplicate cells {}/{} found.", duplicateCells, dtos.size());
        if (failedCells > 0) log.info("Failed saving {}/{} cells.", failedCells, dtos.size());
        System.out.println(" ");
        return savedDtos;
    }

    @Override
    public Cell updateCell(Cell cell) {
        Cell existingCell = cellRepository.findByCellName(cell.getCellName())
                .orElseThrow(() -> new ResourceNotFoundException("Cell", "Cell Name", cell.getCellName()));
        return cellRepository.save(cell);
    }

    @Override
    public CellUpdateResult updateCell(CellDto dto) {       //-- RAT is not updatable

        List<String> warnings = new ArrayList<>();

        Cell cell = findByCellName(dto.cellName());

        if (dto.nodeName() != null) {
            cell.setNodeName(dto.nodeName());
        } else {
            warnings.add("Node name not found");
        }

        if (dto.azimuth() != null) {
            if (geoUtilService.isValidAzimuth(dto.azimuth())) {
                cell.setAzimuth(dto.azimuth());
            } else {
                warnings.add("Invalid azimuth: " + dto.azimuth());
            }
        } else {
            warnings.add("Azimuth not specified");
        }

        if (dto.beamwidth() != null) {
            if (geoUtilService.isValidBeamwidth(dto.beamwidth())) {
                cell.setBeamwidth(dto.beamwidth());
            } else {
                warnings.add("Invalid beamwidth: " + dto.beamwidth());
            }
        } else {
            warnings.add("Beamwidth not specified");
        }

        if (dto.isMultiBeam() != null) {
            cell.setMultiBeam(dto.isMultiBeam());
        } else {
            warnings.add("Multi-Beam not specified");
        }


        if (dto.siteCode() != null) {
            try {
                Site site = siteService.findBySiteCode(dto.siteCode());
                cell.setSite(site);
            } catch (Exception e) {
                log.warn("Site not found by: {}", dto.siteCode());
                warnings.add("Site not found by: " + dto.siteCode());
            }
        } else {
            warnings.add("Site ID not specified");
        }

        if (dto.bandName() != null) {
            try {
                Band band = bandService.findByName(dto.bandName());
                cell.setBand(band);
            } catch (Exception e) {
                log.warn("Band name not found by: {}", dto.bandName());
                warnings.add("Band name not found by: " + dto.bandName());
            }
        } else {
            warnings.add("Band not specified");
        }

        if (dto.carrierName() != null) {
            try {
                Carrier carrier = carrierService.findByName(dto.carrierName());
                cell.setCarrier(carrier);
            } catch (Exception e) {
                log.warn("Carrier name not found by: {}", dto.carrierName());
                warnings.add("Carrier not found by: " + dto.carrierName());
            }
        } else {
            warnings.add("Carrier not specified");
        }

        if (dto.sectorName() != null) {
            try {
                Sector sector = sectorService.findBySectorName(dto.sectorName());
                cell.setSector(sector);
            } catch (ResourceNotFoundException e) {
                log.warn("Sector name not found by: {}", dto.sectorName());

                String[] splitSector = dataTypeUtilService.splitSectorName(dto.sectorName());
                String siteCode = splitSector[0];
                Integer sectorIndex = Integer.parseInt(splitSector[1]);

                if (Objects.equals(siteCode, cell.getSite().getSiteCode())) {
                    Sector sector = Sector.builder()
                            .sectorIndex(sectorIndex)
                            .name(dto.sectorName())
                            .site(cell.getSite())
                            .azimuth(cell.getAzimuth())
                            .build();
                    Sector newSector = sectorService.createSector(sector);
                    cell.setSector(newSector);
                    warnings.add("New sector created " + dto.sectorName());
                } else {
                    warnings.add("Site ID - Sector name mismatch");
                }
            } catch (Exception e) {
                log.warn("Sector modification failed: {}", dto.sectorName());
            }
        } else {
            warnings.add("Sector not specified");
        }

        applyDefaultValues(cell, warnings);

        Cell updatedCell = cellRepository.save(cell);
        CellDto cellDto = mapper.cellToDto(updatedCell);

        return new CellUpdateResult(cellDto, warnings);
    }

    @Override
    public List<CellDto> updateCells(List<CellDto> dtos) {
        List<CellDto> updatedCells = new ArrayList<>();

        for (CellDto dto : dtos) {
            try {
                CellDto updatedCell = updateCell(dto).cellDto();
                updatedCells.add(updatedCell);
            } catch (Exception e) {
                log.warn("Cell update failed by: {} | {}", dto.cellName(), e.getMessage());
            }
        }
        return updatedCells;
    }

    @Override
    public List<CellCsvImportResultDto> updateCellsWithResult(List<CellDto> dtos) {
        List<CellCsvImportResultDto> importResultDtos = new ArrayList<>();

        for (CellDto dto : dtos) {
            try {
                CellUpdateResult result = updateCell(dto);
                String errorMessage = String.join(", ", result.warnings());
                if (result.warnings().isEmpty()) {
                    importResultDtos.add(new CellCsvImportResultDto(result.cellDto(), CsvImportStatus.SUCCESS, ""));
                } else {
                    importResultDtos.add(new CellCsvImportResultDto(result.cellDto(), CsvImportStatus.PARTIAL_SUCCESS, errorMessage));
                }
            } catch (Exception e) {
                log.warn("Error updating cell due to: {}", e.getMessage());
                importResultDtos.add(new CellCsvImportResultDto(dto, CsvImportStatus.FAIL, e.getMessage()));
            }
        }
        return importResultDtos;
    }

    @Override
    public Cell findByCellName(String cellName) {
        return cellRepository.findByCellName(cellName)
                .orElseThrow(() -> new ResourceNotFoundException("Cell", "Cell Name", cellName));
    }

    @Override
    public CellDto getByCellName(String cellName) {
        Cell cell = findByCellName(cellName);
        return mapper.cellToDto(cell);
    }

    @Override
    public List<Cell> findAllCells() {
        return cellRepository.findAll();
    }

    @Override
    public List<CellDto> getAllCells() {
        List<Cell> cells = findAllCells();
        return cells.stream().map(mapper::cellToDto).collect(Collectors.toList());
    }

    @Override
    public List<CellDto> getAllCellInfo() {
        return cellRepository.findAllCells();
    }

    @Override
    public List<CellNameDto> searchCell(String cellName) {
        if (this.allCells.isEmpty()) {
            log.warn("allCells List is empty");
            reloadCells();
        }

        if (cellName == null || cellName.isBlank()) {
            return Collections.emptyList();
        } else {

            List<Rat> rats = ratService.findAll();

            String lower = cellName.toLowerCase();
            List<CellDto> filteredCells = this.allCells.stream()
                    .filter(c -> c.cellName().toLowerCase().contains(lower))
                    .limit(20)
                    .toList();

            return filteredCells.stream()
                    .map(c -> cellDtoToCellNameDto(c, rats))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<CellDto> createLatestCells(String ratName, String granularityName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);

        LocalDateTime timestamp = dateService.getLatestDate(ratName, granularityName)
                .toLocalDate().atStartOfDay().plusSeconds(86399);
        LocalDateTime preTimestamp = timestamp.toLocalDate().atStartOfDay();

        List<CellDto> dtos = kpiDayService.getCellsByTimestamp(timestamp, preTimestamp, rat, granularity);

        return createCells(dtos);
    }

    @Override
    public CompletableFuture<Integer> reloadCells() {
        log.info("Reloading cells...");
        this.allCells = getAllCells();
        log.info("Loaded {} cells", this.allCells.size());
        return CompletableFuture.completedFuture(this.allCells.size());
    }

    @Override
    public CompletableFuture<Integer> reloadCells(String ratName, String granularityName) {

        log.info("[{} - {}] Creating Cells...", ratName, granularityName);
        List<CellDto> cellDtos = createLatestCells(ratName, granularityName);
        log.info("[{} - {}] created {} Cells", granularityName, ratName, cellDtos.size());
        this.allCells = getAllCells();

        return CompletableFuture.completedFuture(this.allCells.size());
    }

    @Override
    public List<Cell> findCellsWithMissingInfo() {
        return cellRepository.findCellsWithMissingInfo();
    }

    @Override
    public List<CellDto> getCellsWithMissingInfo() {
        List<Cell> cells = findCellsWithMissingInfo();
        return cells.stream().map(mapper::cellToDto).collect(Collectors.toList());
    }

    @Override
    public Integer findCellCountWithMissingInfo() {
        this.cellCountWithMissingInfo = cellRepository.findCellCountWithMissingInfo();
        return this.cellCountWithMissingInfo;
    }

    @Override
    public Integer reloadCellCountWithMissingInfo() {
        return findCellCountWithMissingInfo();
    }

    @Override
    public Integer getCellCountWithMissingInfo() {
        return this.cellCountWithMissingInfo;
    }


    private CellNameDto cellDtoToCellNameDto(CellDto dto, List<Rat> rats) {

        Rat rat = rats.stream()
                .filter(r -> dto.ratName().equals(r.getName()))
                .findFirst()
                .orElse(null);

        assert rat != null;
        return new CellNameDto(dto.cellName(), dto.ratName(), rat.getLabel());
    }

    private void applyDefaultValues(Cell cell, List<String> warnings) {
        if (cell.getAzimuth() == null && cell.getSector() != null) {
            cell.setAzimuth(cell.getSector().getAzimuth());
            warnings.add("Azimuth auto added: " + cell.getSector().getAzimuth());
        }

        if (cell.getBeamwidth() == null && !cell.isMultiBeam()) {
            cell.setBeamwidth(65);
            warnings.add("Beamwidth auto added: " + 65);
        }
    }
}
