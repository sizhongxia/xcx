package com.xcx.system.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xcx.system.model.LoveMemory;
import com.xcx.system.util.IdGenerator;
import com.xiaoleilu.hutool.crypto.SecureUtil;
import com.xiaoleilu.hutool.util.StrUtil;

@Controller
@EnableAutoConfiguration
public class IndexController {

	private static final String TOKEN = "wxa759517f581d48e8";

	@Autowired
	private JdbcTemplate jdbcTemplate;

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
		String digest = SecureUtil.sha1(bigStr);
		// 确认请求来至微信
		if (digest.equals(signature)) {
			return echostr;
		}
		return "err";
	}
	

	/**
	 * 获取第一条的记录
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/first")
	@ResponseBody
	Map<String, Object> first() {
		Map<String, Object> data = new HashMap<>();
		data.put("suc", false);
		LoveMemory result = jdbcTemplate.query("select * from tb_love_memory where next_id is null limit 0,1",
				new ResultSetExtractor<LoveMemory>() {
					@Override
					public LoveMemory extractData(ResultSet r) throws SQLException, DataAccessException {
						return convertObj(r);
					}
				});

		if (result == null) {
			data.put("msg", "暂无数据");
			return data;
		}

		data.put("suc", true);
		Map<String, Object> obj = new HashMap<>();
		obj.put("id", result.getId());
		obj.put("picDate", convertPicDate(result.getPicDate()));
		obj.put("picUrl", result.getPicUrl());
		obj.put("picSize", result.getPicSize());
		obj.put("descript", result.getDescript());
		obj.put("preId", result.getPreId());
		obj.put("nextId", result.getNextId());

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		obj.put("createTime", df.format(result.getCreateTime()));
		obj.put("updateTime", df.format(result.getUpdateTime()));

		data.put("data", result);
		return data;
	}
	

	/**
	 * 通过ID获取数据
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/data")
	@ResponseBody
	Map<String, Object> data(String id) {
		Map<String, Object> data = new HashMap<>();
		data.put("suc", false);
		if (StrUtil.isBlank(id)) {
			data.put("msg", "未传入ID");
			return data;
		}
		LoveMemory result = jdbcTemplate.query("select * from tb_love_memory where id=" + id,
				new ResultSetExtractor<LoveMemory>() {
					@Override
					public LoveMemory extractData(ResultSet r) throws SQLException, DataAccessException {
						return convertObj(r);
					}
				});

		if (result == null) {
			data.put("msg", "无效的ID");
			return data;
		}

		data.put("suc", true);
		Map<String, Object> obj = new HashMap<>();
		obj.put("id", result.getId());
		obj.put("picDate", convertPicDate(result.getPicDate()));
		obj.put("picUrl", result.getPicUrl());
		obj.put("picSize", result.getPicSize());
		obj.put("descript", result.getDescript());
		obj.put("preId", result.getPreId());
		obj.put("nextId", result.getNextId());

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		obj.put("createTime", df.format(result.getCreateTime()));
		obj.put("updateTime", df.format(result.getUpdateTime()));

		data.put("data", result);
		return data;
	}

	@RequestMapping("/save")
	@ResponseBody
	Map<String, Object> save(LoveMemory lm) {
		Map<String, Object> data = new HashMap<>();
		data.put("suc", false);
		if (lm == null) {
			data.put("msg", "未传入数据");
			return data;
		}

		if (lm.getPicDate() < 20100101) {
			data.put("msg", "无效的图片时间");
			return data;
		}

		if (StrUtil.isBlank(lm.getPicUrl())) {
			data.put("msg", "请选择一张图片");
			return data;
		}

		if (StrUtil.isBlank(lm.getDescript())) {
			data.put("msg", "请输入图片描述");
			return data;
		}

		String currentId = null;
		try {
			currentId = IdGenerator.getIdGenerator().getId().toString();
		} catch (Exception e) {
		}

		if (currentId == null) {
			data.put("msg", "未生成记录ID");
			return data;
		}

		lm.setId(currentId);

		LoveMemory pre = jdbcTemplate.query("select * from tb_love_memory where pic_date<" + lm.getPicDate()
				+ " order by pic_date desc, id asc limit 0,1", new ResultSetExtractor<LoveMemory>() {
					@Override
					public LoveMemory extractData(ResultSet r) throws SQLException, DataAccessException {
						return convertObj(r);
					}
				});

		if (pre != null) {
			String oldPreNextId = pre.getNextId();
			// 更新Pre
			jdbcTemplate.update("UPDATE `tb_love_memory` SET `next_id`=\"" + currentId + "\" WHERE `id`=\"" + pre.getId() + "\"");

			lm.setPreId(pre.getId());
			lm.setNextId(oldPreNextId);
		} else {
			lm.setPreId(null);
			LoveMemory first = jdbcTemplate.query("select * from tb_love_memory where pre_id is null limit 0,1",
					new ResultSetExtractor<LoveMemory>() {
						@Override
						public LoveMemory extractData(ResultSet r) throws SQLException, DataAccessException {
							return convertObj(r);
						}
					});
			if (first != null) {
				jdbcTemplate
						.update("UPDATE `tb_love_memory` SET `pre_id`=\"" + currentId + "\" WHERE `id`=\"" + first.getId() + "\"");
				lm.setNextId(first.getId());
			} else {
				lm.setNextId(null);
			}
		}

		jdbcTemplate.update(
				"INSERT INTO `tb_love_memory` (`id`, `pre_id`, `next_id`, `pic_date`, `pic_url`, `pic_size`, `descript`, `create_time`, `update_time`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						ps.setString(1, lm.getId());
						ps.setString(2, lm.getPreId());
						ps.setString(3, lm.getNextId());
						ps.setInt(4, lm.getPicDate());
						ps.setString(5, lm.getPicUrl());
						ps.setLong(6, lm.getPicSize());
						ps.setString(7, lm.getDescript());
						ps.setTimestamp(8, new Timestamp((new Date()).getTime()));
						ps.setTimestamp(9, new Timestamp((new Date()).getTime()));
					}
				});

		data.put("suc", true);
		data.put("id", currentId);
		return data;
	}

	@RequestMapping("/update")
	@ResponseBody
	Map<String, Object> update(LoveMemory lm) {
		Map<String, Object> data = new HashMap<>();
		data.put("suc", false);
		if (lm == null) {
			data.put("msg", "未传入数据");
			return data;
		}

		if (StrUtil.isBlank(lm.getId())) {
			data.put("msg", "无效的信息");
			return data;
		}

		if (lm.getPicDate() < 20100101) {
			data.put("msg", "无效的图片时间");
			return data;
		}

		if (StrUtil.isBlank(lm.getPicUrl())) {
			data.put("msg", "请选择一张图片");
			return data;
		}

		if (StrUtil.isBlank(lm.getDescript())) {
			data.put("msg", "请输入图片描述");
			return data;
		}

		try {
			String sql = "UPDATE `tb_love_memory` SET `pic_date`=" + lm.getPicDate() + ", `pic_url`=\"" + lm.getPicUrl()
					+ "\", `pic_size`=" + lm.getPicSize() + ", `descript`=\"" + lm.getDescript() + "\" WHERE `id`=\""
					+ lm.getId() + "\"";
			System.out.println(sql);
			jdbcTemplate.update(sql);
			data.put("suc", true);
			return data;
		} catch (Exception e) {
		}
		data.put("msg", "修改失败");
		return data;
	}

	private LoveMemory convertObj(ResultSet r) throws SQLException, DataAccessException {
		if (r == null || !r.next()) {
			return null;
		}
		LoveMemory lm = new LoveMemory();
		lm.setId(r.getString("id"));
		lm.setPreId(r.getString("pre_id"));
		lm.setNextId(r.getString("next_id"));
		lm.setPicUrl(r.getString("pic_url"));
		lm.setPicSize(r.getLong("pic_size"));
		lm.setPicDate(r.getInt("pic_date"));
		lm.setDescript(r.getString("descript"));
		lm.setCreateTime(r.getDate("create_time"));
		lm.setUpdateTime(r.getDate("update_time"));
		return lm;
	}

	private String convertPicDate(int picDate) {
		String picDateStr = picDate + "";
		String year = picDateStr.substring(0, 4);
		String month = picDateStr.substring(4, 6);
		String day = picDateStr.substring(6, 8);
		return year + "年" + month + "月" + day + "日";
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