package gyullectric.gyullectric.controllter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/errors")
public class ErrorController {

    @GetMapping("/report")
    public String getErrorsReport() {
        return "errors/errorReportList";
    }

    @GetMapping("/guide_id") // 임시 저장주소명
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
    
    @GetMapping("/report/form")
    public String errorReportForm() {
        return "errors/errorReportForm";
    }

}
