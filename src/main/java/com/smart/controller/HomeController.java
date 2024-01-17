package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;


import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	/*
	 * Below is the home page handler
	 * All of functions sends title in model as they need to be used in html
	 */
	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	/*
	 * About page handler
	 */
	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title","About - SCM");
		return "about";
	}
	
	/*
	 * Sign Up page handler
	 * we send a empty user object so that Spring can automatically create user object on basis of form filled
	 */
	@GetMapping("/signup")
	public String signUp(Model m) {
		m.addAttribute("title","Register - SCM");
		m.addAttribute("user",new User());
		return "signup";
	}
	
	/*
	 * Register User
	 * Takes data from signup form and saves it to database to save user to database
	 */
	@PostMapping("/register")
	public String registerUser(
			@Valid @ModelAttribute("user") User user,
			BindingResult br,
			@RequestParam(value = "agreement",defaultValue = "false")boolean agreement,
			Model m,
			HttpSession session
			) {
			
		try {
			if(!agreement) {
				
				throw new Exception("You have not accepted terms and conditions");
				
			}
			
			if(br.hasErrors()) {
				m.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			//encrypting password
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			//encoding ends
			User savedUser = this.userRepository.save(user);
			m.addAttribute("user",new User());	//here we are sending a new empty user. if user wants to create he can create one more user
			session.setAttribute("msg", new Message("Successfully Registered!!","alert-success"));
			
		}
		catch(Exception e) {
			e.printStackTrace();
			session.setAttribute("msg", new Message("Something Went Wrong!!","alert-danger"));
		}
		
		return "signup";
	}
	
	/*
	 * Show login form
	 */
	@GetMapping("/signin")
	public String login(Model m) {
		m.addAttribute("title", "Login - SCM");
		return "login";
	}
}
