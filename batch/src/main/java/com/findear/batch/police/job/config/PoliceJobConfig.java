package com.findear.batch.police.job.config;

import com.findear.batch.police.job.tasklet.PoliceDataMatcingTasklet;
import com.findear.batch.police.job.tasklet.PoliceDataSaveTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PoliceJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job PoliceJob(){

        Job policeJob = jobBuilderFactory.get("policeJob")
                .start(SaveStep())
                .next(MatchingStep())
                .build();

        return policeJob;
    }

    @Bean
    public Step SaveStep(){
        return stepBuilderFactory.get("saveStep")
                .tasklet(new PoliceDataSaveTasklet())
                .build();
    }

    @Bean
    public Step MatchingStep(){
        return stepBuilderFactory.get("matchingStep")
                .tasklet(new PoliceDataMatcingTasklet())
                .build();
    }

}