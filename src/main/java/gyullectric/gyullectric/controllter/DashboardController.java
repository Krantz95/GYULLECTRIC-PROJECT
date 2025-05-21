package gyullectric.gyullectric.controllter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping("/notice")
    public String getNotices() {
        return "notice";
    }

    @GetMapping("/main")
    public String getDashboard () {
        return "dashboard";
    }
}
