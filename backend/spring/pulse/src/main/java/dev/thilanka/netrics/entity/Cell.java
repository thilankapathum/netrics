package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "rat_id", nullable = true)
    private Rat rat;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = true)
    private Site site;

    @ManyToOne
    @JoinColumn(name = "band_id", nullable = true)
    private Band band;
}
