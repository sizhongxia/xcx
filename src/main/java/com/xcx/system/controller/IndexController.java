package com.xcx.system.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

	@RequestMapping("/deploy")
	@ResponseBody
	String deploy() {
		InputStreamReader stdISR = null;
		InputStreamReader errISR = null;
		Process process = null;
		String command = "~/shell/deploy_pro_xcx.sh";
		try {
			process = Runtime.getRuntime().exec(command);
			process.waitFor();

			StringBuffer sb = new StringBuffer();
			String line = null;

			stdISR = new InputStreamReader(process.getInputStream());
			BufferedReader stdBR = new BufferedReader(stdISR);
			while ((line = stdBR.readLine()) != null) {
				// System.out.println("STD line:" + line);
				sb.append(line);
			}

			errISR = new InputStreamReader(process.getErrorStream());
			BufferedReader errBR = new BufferedReader(errISR);
			while ((line = errBR.readLine()) != null) {
				// System.out.println("ERR line:" + line);
				sb.append(line);
			}
			return sb.toString();
		} catch (IOException | InterruptedException e) {
			return "err";
		} finally {
			try {
				if (stdISR != null) {
					stdISR.close();
				}
				if (errISR != null) {
					errISR.close();
				}
				if (process != null) {
					process.destroy();
				}
			} catch (IOException e) {
				return "err";
			}
		}
	}
}