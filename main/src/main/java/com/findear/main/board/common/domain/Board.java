package com.findear.main.board.common.domain;

import com.findear.main.member.common.domain.Member;
import com.findear.main.message.common.domain.MessageRoom;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tbl_board")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<MessageRoom> messageRoomList = new ArrayList<>();

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<Scrap> scrapList = new ArrayList<>();

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<imgFile> imgFileList = new ArrayList<>();

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<ProductColor> productColorList = new ArrayList<>();

    @OneToOne(mappedBy = "board", fetch = FetchType.LAZY)
    private AcquiredBoard acquiredBoard;

    @OneToOne(mappedBy = "board", fetch = FetchType.LAZY)
    private LostBoard lostBoard;

    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private BoardStatus status;

    private Boolean deleteYn;

    private LocalDateTime registeredAt;

    private String thumbnailUrl;

    private String categoryName;

    private String subCategoryName;

}
