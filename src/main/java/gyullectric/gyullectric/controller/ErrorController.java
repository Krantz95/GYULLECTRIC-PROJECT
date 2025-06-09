package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.AnswerForm;
import gyullectric.gyullectric.dto.ErrorReportForm;
import gyullectric.gyullectric.service.ErrorService;
import gyullectric.gyullectric.service.MonitoringDataService;
import gyullectric.gyullectric.service.MonitoringService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
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

//    @GetMapping("/report/form")
//    public String getReport(Model model) {
//        model.addAttribute("errorReportForm", new ErrorReportForm());
//        return "errors/errorReportForm";
//    }
//
//    @PostMapping("report/form")
//    public String postReport(@Valid @ModelAttribute("errorReportForm")ErrorReportForm errorReportForm, BindingResult bindingResult,
//                             HttpSession session){
//
//        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
//
//        if(bindingResult.hasErrors()){
//            return "errors/errorReportForm";
//        }
//        Long processLogId = errorReportForm.getId();
//        ProcessLog processLog = monitoringService.oneFindProcess(processLogId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid processLogId: " + processLogId));
//
//        ErrorReport errorReport = ErrorReport.builder()
//                .errorTitle(errorReportForm.getTitle())
//                .content(errorReportForm.getDescription())
//                .members(loginMember)
//                .processLog(processLog)
//                .writtenAt(LocalDateTime.now())
//                .priority(errorReportForm.getPriority())
//                .occurredAt(errorReportForm.getCreatedAt())
//                .processStep(String.valueOf(errorReportForm.getProcessStep()))
//                .productName(ProductName.valueOf(errorReportForm.getProductName()))
//                .build();
//        errorService.errorSave(errorReport);
//        return "errors/errorReportList";
//
//    }

    @GetMapping("/report/form/{id}")
    public String errorReportForm(Model model,
                                  @PathVariable("id") Long id, HttpSession session) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);



        ProcessLog processLog = monitoringService.oneFindProcess(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공정의 아이디를 찾을 수 없습니다."));

        ErrorReportForm errorReportForm = ErrorReportForm.builder()
                .id(processLog.getId())
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
                .errorCode(ErrorCode.EXCEPTION_ERROR)
                .priority(errorReportForm.getPriority())
                .occurredAt(errorReportForm.getCreatedAt())
                .processStep(String.valueOf(errorReportForm.getProcessStep()))
                .productName(ProductName.valueOf(errorReportForm.getProductName()))
                .build();
         errorService.errorSave(errorReport);
        return "redirect:/errors/report";

    }

    @GetMapping("/detail/{id}")
    public String getReportDetail(Model model,
                                  @PathVariable("id")Long id,
                                  AnswerForm answerForm) {
        Optional<ErrorReport> errorReport = errorService.oneFindError(id);
        if(errorReport.isPresent()){
            model.addAttribute("errorReport", errorReport.get());
            model.addAttribute("answerForm", answerForm);
            return  "errors/errorReportDetail";
        }

        return "errors/errorReportList";
    }

    @GetMapping("/report/delete/{id}")
    public String deleteReport(@PathVariable("id")Long id) {
        Optional<ErrorReport> deleteError = errorService.oneFindError(id);
        errorService.deletError(deleteError.get().getId());
        return "redirect:/errors/report";
    }

    @PostMapping("/answer/{errorId}")
    public String createAnswer(@PathVariable("errorId")Long errorId,
                               @Valid AnswerForm answerForm, BindingResult bindingResult, Model model, HttpSession session) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Optional<ErrorReport> errorReport = errorService.oneFindError(errorId);

        if(bindingResult.hasErrors()){
            model.addAttribute("errorReport", errorReport.get());
            model.addAttribute("loginMember", loginMember);
            return "errors/errorReportList";
        }
        if(errorReport.isPresent()){
            ErrorAnswer errorAnswer = new ErrorAnswer();
            errorAnswer.setContent(answerForm.getContent());
            errorAnswer.setCreateDate(LocalDateTime.now());
            errorAnswer.setMembers(loginMember);
            errorAnswer.setErrorReport(errorReport.get());
            errorService.answerError(errorAnswer);
        }
        model.addAttribute("loginMember", loginMember);
        return String.format("redirect:/errors/detail/%d", errorId);
    }

    @GetMapping("/answer/modify/{answerId}")
    public String modifyAnswer(@PathVariable("answerId")Long answerId, Model model, AnswerForm answerForm) {
        Optional<ErrorAnswer> errorAnswer = errorService.getAnswerById(answerId);
        answerForm.setContent(errorAnswer.get().getContent());

        model.addAttribute("answerForm", answerForm);
        return "errors/errorAnswerUpdate";
    }

    @PostMapping("/answer/modify/{answerId}")
    public String postModifyAnswer(@PathVariable("answerId")Long answerId, Model model,@Valid AnswerForm answerForm, BindingResult bindingResult, HttpSession session) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(bindingResult.hasErrors()){
            return "errors/errorAnswerUpdate";
        }
        Optional<ErrorAnswer> errorAnswers = errorService.getAnswerById(answerId);
        log.info("찾은 답글 번호 : {}", errorAnswers.get().getId());

        Optional<ErrorReport> errorReport = errorService.oneFindError(errorAnswers.get().getErrorReport().getId());
        log.info("찾은 에러 번호 : {}", errorReport.get().getId());

        ErrorAnswer errorAnswer = new ErrorAnswer();
        errorAnswer.setId(answerId);
        errorAnswer.setCreateDate(errorAnswers.get().getCreateDate());
        errorAnswer.setContent(answerForm.getContent());
        errorAnswer.setModifyDate(LocalDateTime.now());
        errorAnswer.setMembers(loginMember);
        errorAnswer.setErrorReport(errorReport.get());
        errorService.answerError(errorAnswer);
        return String.format("redirect:/errors/detail/%s", errorReport.get().getId());
    }
    @GetMapping("/answer/delete/{answerId}")
    public String deleteReportAnswer(@PathVariable("answerId")Long answerId) {
        Optional<ErrorAnswer> errorAnswer = errorService.getAnswerById(answerId);


        if(!errorAnswer.isPresent()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found");
        }

        ErrorAnswer errorAnswer1 = errorAnswer.get();
        Long id = errorAnswer1.getErrorReport().getId();

        errorService.deleteAnswerById(errorAnswer1.getId());

        return String.format("redirect:/errors/detail/%s", id);
    }
}
