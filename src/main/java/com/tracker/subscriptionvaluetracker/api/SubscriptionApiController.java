package com.tracker.subscriptionvaluetracker.api;

import com.tracker.subscriptionvaluetracker.common.UserIdentifier;
import com.tracker.subscriptionvaluetracker.domain.subscription.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Subscription", description = "구독 관리 API")
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionApiController {

    private final SubscriptionService subscriptionService;

    public SubscriptionApiController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Operation(summary = "구독 목록 조회", description = "사용자의 모든 활성 구독 목록을 통계와 함께 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionViewDto>>> getSubscriptions(
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        List<SubscriptionViewDto> subscriptions = subscriptionService.getSubscriptionsWithStats(userUuid);
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @Operation(summary = "구독 상세 조회", description = "특정 구독의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "구독을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionViewDto>> getSubscription(
            @Parameter(description = "구독 ID", required = true) @PathVariable Long id,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            SubscriptionViewDto subscription = subscriptionService.getSubscriptionWithStats(id, userUuid);
            return ResponseEntity.ok(ApiResponse.success(subscription));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "구독 등록", description = "새로운 구독을 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionViewDto>> createSubscription(
            @RequestBody SubscriptionForm form,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            Subscription subscription = subscriptionService.createSubscription(userUuid, form);
            SubscriptionViewDto dto = subscriptionService.toViewDto(subscription);
            return ResponseEntity.ok(ApiResponse.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "구독 수정", description = "기존 구독 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "구독을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionViewDto>> updateSubscription(
            @Parameter(description = "구독 ID", required = true) @PathVariable Long id,
            @RequestBody SubscriptionForm form,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            Subscription subscription = subscriptionService.updateSubscription(id, userUuid, form);
            SubscriptionViewDto dto = subscriptionService.toViewDto(subscription);
            return ResponseEntity.ok(ApiResponse.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "구독 삭제", description = "구독을 삭제합니다. (soft delete)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "구독을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscription(
            @Parameter(description = "구독 ID", required = true) @PathVariable Long id,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            subscriptionService.deleteSubscription(id, userUuid);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "출석 체크 (토글)", description = "해당 날짜에 출석 체크합니다. 이미 출석한 경우 취소됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "출석 체크/취소 성공")
    })
    @PostMapping("/{id}/check-in")
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(
            @Parameter(description = "구독 ID", required = true) @PathVariable Long id,
            @Parameter(description = "출석 날짜 (YYYY-MM-DD, 미입력 시 오늘)") @RequestParam(required = false) String date,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        LocalDate checkInDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();

        boolean checkedIn = subscriptionService.toggleCheckIn(id, userUuid, checkInDate);
        SubscriptionViewDto updatedSubscription = subscriptionService.getSubscriptionWithStats(id, userUuid);

        CheckInResponse result = new CheckInResponse(checkedIn, updatedSubscription);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "사용 기록 조회", description = "구독의 최근 사용 기록을 조회합니다.")
    @GetMapping("/{id}/usage")
    public ResponseEntity<ApiResponse<List<UsageLog>>> getUsageLogs(
            @Parameter(description = "구독 ID", required = true) @PathVariable Long id,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        // 권한 확인
        subscriptionService.getSubscription(id, userUuid)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));

        List<UsageLog> logs = subscriptionService.getRecentUsageLogs(id);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @Schema(description = "출석 체크 응답")
    public record CheckInResponse(
            @Schema(description = "출석 여부 (true: 출석, false: 취소)") boolean checkedIn,
            @Schema(description = "업데이트된 구독 정보") SubscriptionViewDto subscription
    ) {}
}
