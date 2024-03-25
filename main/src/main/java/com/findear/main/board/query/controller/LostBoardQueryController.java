package com.findear.main.board.query.controller;

import com.findear.main.board.query.service.LostBoardQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/board")
@RestController
public class LostBoardQueryController {

    private final LostBoardQueryService lostBoardQueryService;


}
