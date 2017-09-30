package com.xcx.system.controller;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@EnableAutoConfiguration
public class IndexController {

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello World";
	}

	@RequestMapping("/check")
	@ResponseBody
	String check() {
		return "Check";
	}

	@RequestMapping("/zx")
	@ResponseBody
	String check2() {
		return "Zhongxia";
	}
}