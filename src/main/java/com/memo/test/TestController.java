package com.memo.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.memo.post.mapper.PostMapper;

@Controller
public class TestController {

	@Autowired
	private PostMapper postMapper;
	
	// http://localhost/test1
	@GetMapping("/test1")
	@ResponseBody
	public String test1() {
		return "hello world";
	}

	// http://localhost/test2
	@GetMapping("/test2")
	@ResponseBody
	public Map<String, Object> test2() {
		Map<String, Object> result = new HashMap<>();
		result.put("aa", 11);
		result.put("bb", 11);
		result.put("cc", 11);
		return result;
	}
	
	// http://localhost/test3
	@GetMapping("/test3")
	public String test3() {
		return "test/test";
	}
	
	// http://localhost/test4
	@GetMapping("/test4")
	@ResponseBody
	public List<Map<String, Object>> test4() {
		return postMapper.selectPostList();
	}
}
