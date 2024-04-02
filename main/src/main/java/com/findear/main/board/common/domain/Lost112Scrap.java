package com.findear.main.board.common.domain;

import com.findear.main.member.common.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tbl_lost112_scrap")
public class Lost112Scrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lost112_scrap_id")
    private Long id;

    @Column(name = "lost112_board_id")
    @JoinColumn(name = "lost112_board_id")
    private Long lost112BoardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public Lost112Scrap(Long lost112BoardId, Member member) {
        this.lost112BoardId = lost112BoardId;
        this.member = member;
    }
}
