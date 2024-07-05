package com.nanobot.bzzstromzaehlerservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class WebController {

    @GetMapping("/")
    public String home(){
        return "dia_1";
    }

    @GetMapping("/script")
    public String script() {
        return "script.js";
    }
}
