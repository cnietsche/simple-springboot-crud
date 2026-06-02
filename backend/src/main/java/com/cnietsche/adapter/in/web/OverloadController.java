package com.cnietsche.adapter.in.web;

import com.cnietsche.adapter.in.web.dto.GenerateOverloadRequest;
import com.cnietsche.adapter.in.web.dto.GenerateOverloadResponse;
import com.cnietsche.adapter.in.web.dto.OverloadPageResponse;
import com.cnietsche.adapter.in.web.dto.OverloadResponse;
import com.cnietsche.adapter.in.web.dto.OverloadStatisticsResponse;
import com.cnietsche.adapter.in.web.dto.TimeSeriesBucketResponse;
import com.cnietsche.adapter.in.web.dto.UserOverloadCountResponse;
import com.cnietsche.application.service.RecordBatchSizeMapper;
import com.cnietsche.domain.model.StatisticsPeriod;
import com.cnietsche.domain.port.in.GenerateOverloadCommand;
import com.cnietsche.domain.port.in.GenerateOverloadUseCase;
import com.cnietsche.domain.port.in.GetOverloadStatisticsUseCase;
import com.cnietsche.domain.port.in.ListOverloadUseCase;
import com.cnietsche.domain.port.in.OverloadPageView;
import com.cnietsche.domain.port.in.OverloadStatisticsView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/overloads")
public class OverloadController {

    private final GenerateOverloadUseCase generateOverloadUseCase;
    private final ListOverloadUseCase listOverloadUseCase;
    private final GetOverloadStatisticsUseCase getOverloadStatisticsUseCase;

    public OverloadController(
            GenerateOverloadUseCase generateOverloadUseCase,
            ListOverloadUseCase listOverloadUseCase,
            GetOverloadStatisticsUseCase getOverloadStatisticsUseCase) {
        this.generateOverloadUseCase = generateOverloadUseCase;
        this.listOverloadUseCase = listOverloadUseCase;
        this.getOverloadStatisticsUseCase = getOverloadStatisticsUseCase;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerateOverloadResponse> generate(@Valid @RequestBody GenerateOverloadRequest request) {
        int created = generateOverloadUseCase.execute(
                new GenerateOverloadCommand(
                        request.userId(),
                        RecordBatchSizeMapper.fromCount(request.count())));
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenerateOverloadResponse(created));
    }

    @GetMapping
    public OverloadPageResponse list(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        OverloadPageView view = listOverloadUseCase.execute(userId, page, size);
        return new OverloadPageResponse(
                view.content().stream()
                        .map(v -> new OverloadResponse(v.id(), v.date(), v.userId(), v.value()))
                        .toList(),
                view.page(),
                view.size(),
                view.totalElements(),
                view.totalPages()
        );
    }

    @GetMapping("/statistics")
    public OverloadStatisticsResponse statistics(
            @RequestParam StatisticsPeriod period,
            @RequestParam(required = false) UUID userId) {
        OverloadStatisticsView view = getOverloadStatisticsUseCase.execute(period, userId);
        return new OverloadStatisticsResponse(
                view.topUsers().stream()
                        .map(u -> new UserOverloadCountResponse(u.userId(), u.userName(), u.count()))
                        .toList(),
                view.timeSeries().stream()
                        .map(b -> new TimeSeriesBucketResponse(b.bucketStart(), b.count()))
                        .toList()
        );
    }
}
