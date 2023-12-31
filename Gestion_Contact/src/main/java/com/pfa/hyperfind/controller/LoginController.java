package com.pfa.hyperfind.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.pfa.hyperfind.entity.User;

@Controller
public class LoginController {
	
	@GetMapping("/login")
	public String getLogin(Model model) {
		model.addAttribute("user", new User());
		System.out.println("Login entered");
		return "login";
	}
	
	
}
