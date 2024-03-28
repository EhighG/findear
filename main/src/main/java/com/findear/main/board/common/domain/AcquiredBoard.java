package com.findear.main.board.common.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
    private LocalDate acquiredAt;

    private String address;

    private String name;

    private Float xPos;

    private Float yPos;

    public void updateAutoFilledColumn(Board board) {
        this.board.updateAutofillColumn(board);
    }
}
