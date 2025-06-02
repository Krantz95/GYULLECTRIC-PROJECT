package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.ErrorReport;
import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.domain.ProcessLog;
import gyullectric.gyullectric.dto.ErrorReportForm;
import gyullectric.gyullectric.service.ErrorService;
import gyullectric.gyullectric.service.MonitoringDataService;
import gyullectric.gyullectric.service.MonitoringService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/errors")
public class ErrorController {
    private final MonitoringService monitoringService;
    private final MonitoringDataService monitoringDataService;
    private final ErrorService errorService;

    @GetMapping("/report")
    public String getErrorsReport(Model model) {
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

    @GetMapping("/report_id") // 임시 저장주소명
    public String getReportDetail() {
        return "errors/errorReportDetail";
    }

    @GetMapping("/report/form/{id}")
    public String errorReportForm(Model model,
                                  @PathVariable("id") Long id, HttpSession session) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);

        ProcessLog processLog = monitoringService.oneFindProcess(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공정의 아이디를 찾을 수 없습니다."));

        ErrorReport savedErrorReport;

        if (processLog.getId() != null) {
            ErrorReport newReport = new ErrorReport();
            newReport.setErrorTitle("EXCEPTION_ERROR");
            newReport.setProcessLog(processLog);
            newReport.setMembers(processLog.getOrderList().getMembers());
            newReport.setOccurredAt(processLog.getCreateAt());
            newReport.setProductName(processLog.getProductName());

            savedErrorReport = errorService.errorSave(newReport);
        } else {
            throw new IllegalArgumentException("공정 ID가 null입니다.");
        }

        ErrorReportForm errorReportForm = ErrorReportForm.builder()
                .title("EXCEPTION_ERROR 예외 에러 발생 보고")
                .errorCode("EXCEPTION_ERROR")
                .authorName(loginMember.getName())
                .createdAt(savedErrorReport.getOccurredAt().toString())
                .processStep(processLog.getProcessStep())
                .build();

        model.addAttribute("errorReportForm", errorReportForm);
        return "errors/errorReportForm";

    }
}