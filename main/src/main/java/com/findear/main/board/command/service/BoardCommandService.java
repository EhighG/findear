package com.findear.main.board.command.service;

import com.findear.main.board.command.repository.BoardCommandRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class BoardCommandService {

    private final BoardCommandRepository boardCommandRepository;

}