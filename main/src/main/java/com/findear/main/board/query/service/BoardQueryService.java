package com.findear.main.board.query.service;


import com.findear.main.board.query.repository.BoardQueryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class BoardQueryService {

    private final BoardQueryRepository boardQueryRepository;
}
