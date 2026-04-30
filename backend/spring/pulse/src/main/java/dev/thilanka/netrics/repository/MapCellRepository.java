package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.MapCell;
import dev.thilanka.netrics.dto.SiteDto;
import dev.thilanka.netrics.entity.KpiDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MapCellRepository extends JpaRepository<KpiDay, Long> {

    @Query(value = """
            SELECT s.site_code,
                s.site_name,
                kv.cell_name,
                s.latitude,
                s.longitude,
                c.azimuth,
                c.beamwidth,
                skpi.label as kpi_label,
                kv.kpi_value,
                car.radius
            FROM kpi_values kv
            LEFT JOIN cells c ON c.cell_name = kv.cell_name
            LEFT JOIN sites s ON s.id = c.site_id
            LEFT JOIN carriers car ON car.id = c.carrier_id
            LEFT JOIN standard_kpi skpi ON skpi.id = kv.standard_kpi_id
            JOIN district_codes dc ON dc.id = kv.district_code_id
            JOIN area_district_code_mapping adcm ON adcm.district_code_id = dc.id
            JOIN areas ar ON ar.id = adcm.area_id
            WHERE kv.standard_kpi_id = :standardKpiId
                AND kv.rat_id = :ratId
                AND kv.granularity_id = :granularityId
                AND s.geom && ST_MakeEnvelope(:minLng,:minLat, :maxLng, :maxLat, 4326)
                AND kv.timestamp BETWEEN :startTime AND :endTime
                AND ar.id = :areaId
                and c.azimuth is not null;
            """, nativeQuery = true)
    List<MapCell> queryCellsByKpi(
            @Param("minLng") Double minLng,
            @Param("minLat") Double minLat,
            @Param("maxLng") Double maxLng,
            @Param("maxLat") Double maxLat,
            @Param("standardKpiId") Long standardKpiId,
            @Param("ratId") Long ratId,
            @Param("granularityId") Long granularityId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("areaId") Long areaId
    );


    @Query(value = """
            SELECT
                s.site_code,
                s.site_name,
                kv.cell_name,
                s.latitude,
                s.longitude,
                c.azimuth,
                c.beamwidth,
                skpi.label AS kpi_label,
                kv.kpi_value,
                car.radius
            FROM kpi_values kv
            JOIN cells c         ON c.cell_name = kv.cell_name
            JOIN sites s         ON s.id = c.site_id
            JOIN carriers car    ON car.id = c.carrier_id
            JOIN standard_kpi skpi ON skpi.id = kv.standard_kpi_id
            JOIN district_codes dc         ON dc.id = kv.district_code_id
            JOIN area_district_code_mapping adcm ON adcm.district_code_id = dc.id
            JOIN areas ar        ON ar.id = adcm.area_id
            WHERE kv.standard_kpi_id  = :standardKpiId
                AND kv.rat_id            = :ratId
                AND kv.granularity_id    = :granularityId
                AND kv.timestamp BETWEEN :startTime AND :endTime
                AND ar.id                = :areaId
                AND c.azimuth IS NOT NULL
                AND s.geom && ST_Expand(
                    ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326),
                    :bufferDegrees   -- bleeds sectors near tile edges into neighbour tiles
                )
            """, nativeQuery = true)
    List<MapCell> queryCellsByKpiTile(
            @Param("minLng") Double minLng,
            @Param("minLat") Double minLat,
            @Param("maxLng") Double maxLng,
            @Param("maxLat") Double maxLat,
            @Param("bufferDegrees") Double bufferDegrees,
            @Param("standardKpiId") Long standardKpiId,
            @Param("ratId") Long ratId,
            @Param("granularityId") Long granularityId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("areaId") Long areaId
    );


    @Query(value = """
            SELECT
                s.site_code,
                s.site_name,
                s.latitude,
                s.longitude
            FROM sites s
            WHERE s.geom && ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326)
            """, nativeQuery = true)
    List<SiteDto> querySitesByTile(
            @Param("minLng") Double minLng,
            @Param("minLat") Double minLat,
            @Param("maxLng") Double maxLng,
            @Param("maxLat") Double maxLat
    );


    @Query(value = """
                    WITH tile AS (
                        SELECT ST_TileEnvelope(:z, :x, :y) AS geom
                    ),
                    mvtgeom AS (
                        SELECT DISTINCT ON (kv.cell_name)
                            s.site_code,
                            s.site_name,
                            kv.cell_name,
                            s.latitude,
                            s.longitude,
                            c.azimuth,
                            c.beamwidth,
                            skpi.label AS kpi_label,
                            kv.kpi_value,
                            car.radius,
                            ST_AsMVTGeom(
                                s.geom,
                                tile.geom,
                                4096,
                                256,
                                true
                            ) AS geom
                        FROM kpi_values kv
                        LEFT JOIN cells c ON c.cell_name = kv.cell_name
                        LEFT JOIN sites s ON s.id = c.site_id
                        LEFT JOIN carriers car ON car.id = c.carrier_id
                        LEFT JOIN standard_kpi skpi ON skpi.id = kv.standard_kpi_id
                        JOIN district_codes dc ON dc.id = kv.district_code_id
                        JOIN area_district_code_mapping adcm ON adcm.district_code_id = dc.id
                        JOIN areas ar ON ar.id = adcm.area_id
                        JOIN tile ON s.geom && tile.geom
                        WHERE kv.standard_kpi_id = :standardKpiId
                            AND kv.rat_id = :ratId
                            AND kv.granularity_id = :granularityId
                            AND kv.timestamp BETWEEN :startTime AND :endTime
                            AND ar.id = :areaId
                            AND c.azimuth IS NOT NULL
                    )
                    SELECT ST_AsMVT(mvtgeom, 'cells', 4096, 'geom');
            """, nativeQuery = true)
    byte[] getTile(@Param("z") int z,
                   @Param("x") int x,
                   @Param("y") int y,
                   @Param("standardKpiId") Long standardKpiId,
                   @Param("ratId") Long ratId,
                   @Param("granularityId") Long granularityId,
                   @Param("startTime") LocalDateTime startTime,
                   @Param("endTime") LocalDateTime endTime,
                   @Param("areaId") Long areaId);
}
