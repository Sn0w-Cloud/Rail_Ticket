package org.zerock.project.Controller;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seat")
public class SeatController {

    @GetMapping("")
    public String seatPage() {
        return "seat";
    }


}
