package com.xcx.system.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xiaoleilu.hutool.crypto.SecureUtil;
import com.xiaoleilu.hutool.util.StrUtil;

@Controller
@EnableAutoConfiguration
public class IndexController {

	private static final String TOKEN = "wxa759517f581d48e8";

	@RequestMapping("/")
	@ResponseBody
	String home(HttpServletRequest request) {
		// 微信加密签名
		String signature = request.getParameter("signature");
		if (StrUtil.isBlank(signature)) {
			return "signature is blank";
		}
		// 随机字符串
		String echostr = request.getParameter("echostr");
		if (StrUtil.isBlank(echostr)) {
			return "echostr is blank";
		}
		// 时间戳
		String timestamp = request.getParameter("timestamp");
		if (StrUtil.isBlank(timestamp)) {
			return "timestamp is blank";
		}
		// 随机数
		String nonce = request.getParameter("nonce");
		if (StrUtil.isBlank(nonce)) {
			return "nonce is blank";
		}

		String[] str = { TOKEN, timestamp, nonce };
		Arrays.sort(str); // 字典序排序
		String bigStr = str[0] + str[1] + str[2];

		System.out.println(bigStr);
		String digest = SecureUtil.sha1(bigStr);
		System.out.println(digest);
		System.out.println(signature);
		System.out.println(echostr);
		// 确认请求来至微信
		if (digest.equals(signature)) {
			return echostr;
		}
		return "err";
	}

	@RequestMapping("/data")
	@ResponseBody
	Map<String, Object> data() {
		Map<String, Object> data = new HashMap<>();
		data.put("suc", true);
		data.put("name", "sizhongxia");
		return data;
	}

	@RequestMapping("/deploy")
	@ResponseBody
	String deploy() {
		InputStreamReader stdISR = null;
		InputStreamReader errISR = null;
		Process process = null;
		String command = "/root/shell/deploy_pro_xcx.sh";
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
			return "suc - " + sb.toString();
		} catch (IOException | InterruptedException e) {
			return e.getMessage();
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