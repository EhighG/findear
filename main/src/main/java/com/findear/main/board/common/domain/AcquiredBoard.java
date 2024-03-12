package com.findear.main.board.common.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tbl_acquired_board")
public class AcquiredBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acquired_board_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @OneToMany(mappedBy = "acquiredBoard", fetch = FetchType.LAZY)
    private List<ReturnLog> returnLogList = new ArrayList<>();

    private LocalDate acquiredAt;

    private String phoneNumber;

    private String address;

    private String name;

    private Float xPos;

    private Float yPos;

}
