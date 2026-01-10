package com.tracker.subscriptionvaluetracker.api;

import com.tracker.subscriptionvaluetracker.common.UserIdentifier;
import com.tracker.subscriptionvaluetracker.domain.investment.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Investment", description = "투자 항목 관리 API")
@RestController
@RequestMapping("/api/v1/investments")
public class InvestmentApiController {

    private final InvestmentService investmentService;

    public InvestmentApiController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @Operation(summary = "투자 목록 조회", description = "사용자의 모든 활성 투자 항목 목록을 통계와 함께 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<InvestmentViewDto>>> getInvestments(
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        List<InvestmentViewDto> investments = investmentService.getInvestmentsWithStats(userUuid);
        return ResponseEntity.ok(ApiResponse.success(investments));
    }

    @Operation(summary = "투자 상세 조회", description = "특정 투자 항목의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "투자 항목을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvestmentViewDto>> getInvestment(
            @Parameter(description = "투자 ID", required = true) @PathVariable Long id,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            InvestmentViewDto investment = investmentService.getInvestmentWithStats(id, userUuid);
            return ResponseEntity.ok(ApiResponse.success(investment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "투자 등록", description = "새로운 투자 항목을 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<InvestmentViewDto>> createInvestment(
            @RequestBody InvestmentForm form,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            Investment investment = investmentService.createInvestment(userUuid, form);
            InvestmentViewDto dto = investmentService.toViewDto(investment);
            return ResponseEntity.ok(ApiResponse.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "투자 수정", description = "기존 투자 항목 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "투자 항목을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvestmentViewDto>> updateInvestment(
            @Parameter(description = "투자 ID", required = true) @PathVariable Long id,
            @RequestBody InvestmentForm form,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            Investment investment = investmentService.updateInvestment(id, userUuid, form);
            InvestmentViewDto dto = investmentService.toViewDto(investment);
            return ResponseEntity.ok(ApiResponse.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "투자 삭제", description = "투자 항목을 삭제합니다. (soft delete)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "투자 항목을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvestment(
            @Parameter(description = "투자 ID", required = true) @PathVariable Long id,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            investmentService.deleteInvestment(id, userUuid);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "사용 기록 추가", description = "투자 항목에 새로운 사용 기록을 추가합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추가 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "투자 항목을 찾을 수 없음")
    })
    @PostMapping("/{id}/usage")
    public ResponseEntity<ApiResponse<InvestmentUsage>> addUsage(
            @Parameter(description = "투자 ID", required = true) @PathVariable Long id,
            @RequestBody InvestmentUsageForm form,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            InvestmentUsage usage = investmentService.addUsage(id, userUuid, form);
            return ResponseEntity.ok(ApiResponse.success(usage));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "사용 기록 조회", description = "투자 항목의 사용 기록을 조회합니다.")
    @GetMapping("/{id}/usage")
    public ResponseEntity<ApiResponse<List<InvestmentUsage>>> getUsageLogs(
            @Parameter(description = "투자 ID", required = true) @PathVariable Long id,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            List<InvestmentUsage> logs = investmentService.getUsageLogs(id, userUuid);
            return ResponseEntity.ok(ApiResponse.success(logs));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "사용 기록 삭제", description = "특정 사용 기록을 삭제합니다.")
    @DeleteMapping("/{id}/usage/{usageId}")
    public ResponseEntity<ApiResponse<Void>> deleteUsage(
            @Parameter(description = "투자 ID", required = true) @PathVariable Long id,
            @Parameter(description = "사용 기록 ID", required = true) @PathVariable Long usageId,
            HttpServletRequest request,
            HttpServletResponse response) {
        String userUuid = UserIdentifier.getUserUuid(request, response);
        try {
            investmentService.deleteUsage(id, usageId, userUuid);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
}
