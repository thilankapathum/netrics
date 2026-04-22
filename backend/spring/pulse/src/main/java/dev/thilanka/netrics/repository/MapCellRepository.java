package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.dto.MapCell;
import dev.thilanka.netrics.entity.KpiDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            WHERE kv.standard_kpi_id = :standardKpiId
                AND kv.rat_id = :ratId
                AND kv.granularity_id = :granularityId
                AND s.geom && ST_MakeEnvelope(:minLng,:minLat, :maxLng, :maxLat, 4326)
                and kv.timestamp = '2026-02-23'
                and c.azimuth is not null
            """, nativeQuery = true)
    List<MapCell> queryCellsByKpi(
            @Param("minLng") Double minLng,
            @Param("minLat") Double minLat,
            @Param("maxLng") Double maxLng,
            @Param("maxLat") Double maxLat,
            @Param("standardKpiId") Long standardKpiId,
            @Param("ratId") Long ratId,
            @Param("granularityId") Long granularityId
    );
}
