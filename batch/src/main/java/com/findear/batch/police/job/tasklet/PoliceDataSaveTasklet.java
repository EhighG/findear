package com.findear.batch.police.job.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PoliceDataSaveTasklet implements Tasklet, StepExecutionListener {

    @Override
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        log.info("경찰청 데이터 저장 Start!");
    }

    @Override
    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {

        log.info("경찰청 데이터 저장 End!");

        return ExitStatus.COMPLETED;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        //비즈니스 로직

        return RepeatStatus.FINISHED;
    }

}