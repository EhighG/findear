package com.findear.main.board.query.controller;

import com.findear.main.board.query.service.BoardQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/board")
@RestController
public class BoardQueryController {

    private final BoardQueryService boardQueryService;

}
