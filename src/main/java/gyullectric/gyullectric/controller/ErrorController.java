package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.ErrorReportForm;
import gyullectric.gyullectric.service.ErrorService;
import gyullectric.gyullectric.service.MonitoringDataService;
import gyullectric.gyullectric.service.MonitoringService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/errors")
public class ErrorController {
    private final MonitoringService monitoringService;
    private final MonitoringDataService monitoringDataService;
    private final ErrorService errorService;

    @GetMapping("/report")
    public String getErrorsReport(Model model, HttpSession session,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "kw", defaultValue = "") String kw,
                                  @RequestParam(value = "type", required = false, defaultValue = "") String type) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);

        Page<ErrorReport> paging = errorService.getList(page, kw, type);
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("type", type);
        return "errors/errorReportList";
    }

    @GetMapping("/errorGuideDetail") // 주소변경 (view로만 보여줄것)
    public String getGuideDetail() {
        return "errors/errorGuideDetail";
    }

    @GetMapping("/guide")
    public String getGuideList() {
        return "errors/errorGuideList";
    }

    @GetMapping("/report/form") // 임시 저장주소명
    public String getReport(Model model) {
        model.addAttribute("errorReportForm", new ErrorReportForm());
        return "errors/errorReportForm";
    }

    @GetMapping("/recent-exceptions")
    public ResponseEntity<List<ErrorReport>> getRecentExceptionErrors(
            @RequestParam(defaultValue = "5") int limit,
            Model model) {
        List<ErrorReport> errors = errorService.getRecentExceptionErrors(limit);
        model.addAttribute("errorReportForm", new ErrorReportForm());
        return ResponseEntity.ok(errors);
    }
    @PostMapping("report/form")
    public String postReport(@Valid @ModelAttribute("errorReportForm")ErrorReportForm errorReportForm, BindingResult bindingResult,
                             HttpSession session){

        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if(bindingResult.hasErrors()){
            return "errors/errorReportForm";
        }
        Long processLogId = errorReportForm.getId();
        ProcessLog processLog = monitoringService.oneFindProcess(processLogId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid processLogId: " + processLogId));

        ErrorReport errorReport = ErrorReport.builder()
                .errorTitle(errorReportForm.getTitle())
                .content(errorReportForm.getDescription())
                .members(loginMember)
                .processLog(processLog)
                .writtenAt(LocalDateTime.now())
                .priority(errorReportForm.getPriority())
                .occurredAt(errorReportForm.getCreatedAt())
                .processStep(String.valueOf(errorReportForm.getProcessStep()))
                .productName(ProductName.valueOf(errorReportForm.getProductName()))
                .build();
        errorService.errorSave(errorReport);
        return "errors/errorReportList";

    }

    @GetMapping("/report/form/{id}")
    public String errorReportForm(Model model,
                                  @PathVariable("id") Long id, HttpSession session) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);



        ProcessLog processLog = monitoringService.oneFindProcess(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공정의 아이디를 찾을 수 없습니다."));

        ErrorReportForm errorReportForm = ErrorReportForm.builder()
                .title(processLog.getErrorCode() + "예외 에러 코드 발생")
                .productName(processLog.getProductName().name())
                .processStep(processLog.getProcessStep())
                .createdAt(processLog.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")))
                .build();
        model.addAttribute("errorReportForm", errorReportForm);

        return "errors/errorReportForm";

    }

    @PostMapping("/report/form/{id}")
    public String postReportForm(@PathVariable("id")Long id, Model model,
                                 @Valid @ModelAttribute("errorReportForm")ErrorReportForm errorReportForm, BindingResult bindingResult,
                                 HttpSession session){
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if(bindingResult.hasErrors()){
            return "errors/errorReportForm";
        }
        Long processId = errorReportForm.getId(); // 폼에서 받아야 함
        ProcessLog processLog = monitoringService.oneFindProcess(processId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 프로세스 ID입니다."));

        ErrorReport errorReport = ErrorReport.builder()
                .errorTitle(errorReportForm.getTitle())
                .content(errorReportForm.getDescription())
                .members(loginMember)
                .processLog(processLog)
                .writtenAt(LocalDateTime.now())
                .priority(errorReportForm.getPriority())
                .occurredAt(errorReportForm.getCreatedAt())
                .processStep(String.valueOf(errorReportForm.getProcessStep()))
                .productName(ProductName.valueOf(errorReportForm.getProductName()))
                .build();
         errorService.errorSave(errorReport);
        return "errors/errorReportList";

    }
}
