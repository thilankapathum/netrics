package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cells")
public class Cell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String cellName;
    @Column(nullable = true)
    private String nodeName;

    @Max(360)
    @Min(0)
    private Integer azimuth;

    @Max(360)
    @Min(0)
    private Integer beamwidth;

    private boolean isMultiBeam;

    @ManyToOne
    @JoinColumn(name = "rat_id", nullable = true)
    private Rat rat;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = true)
    private Site site;

    @ManyToOne
    @JoinColumn(name = "band_id", nullable = true)
    private Band band;

    @ManyToOne
    @JoinColumn(name = "carrier_id", nullable = true)
    private Carrier carrier;

    @ManyToOne
    @JoinColumn(name = "sector_id", nullable = true)
    private Sector sector;
}
