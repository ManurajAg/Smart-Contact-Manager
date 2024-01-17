package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
 	
	Random random = new Random(1000);
	//Email id form open handler
	@GetMapping("/forgot")
	public String openEmailForm(Model m) {
		m.addAttribute("title", "Forgot Password");
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email,HttpSession s,Model m){
		//generate otp
		
		int otp = random.nextInt(999999);
		String subject = "OTP form SCM";
		String message = ""+
					"<div style ='border:1px solid #e2e2e2; padding:20px;'>"
					+"<h1>OTP for Smart Contact Manager is :- <strong>"+otp+"</strong></h1>"
					+"</div>";
		String to = email;
		boolean flag = emailService.sendEmail(subject, message,to);
		if(flag) {
			s.setAttribute("myotp", otp);
			s.setAttribute("email", to);
			s.setAttribute("msg", new Message("OTP Sent Successfully!!","success"));
			m.addAttribute("title","Verify OTP");
			return "verify-otp";
		}
		else {
			s.setAttribute("msg", new Message("Check your Email Id !!","danger"));
		}
		return "forgot_email_form";
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp")Integer otp,HttpSession s,Model m) {
		Integer myOtp = (int)s.getAttribute("myotp");
		String email =(String)s.getAttribute("email");
		System.out.println(myOtp);
		System.out.println(otp);
		if(myOtp.equals(otp)) {
			//change password page
			User user = this.userRepository.getUserByName(email);
			if(user==null) {
				s.setAttribute("msg",new Message("Invalid User","danger"));
				m.addAttribute("title", "New Password");
				return "forgot_email_form";
			}
			
			return "password_change_form";
		}
		else {
			//verify-otp.html
			m.addAttribute("title", "Verify OTP");
			s.setAttribute("msg", new Message("Invalid OTP","danger"));
			return "verify-otp";	
		}
		
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String password,HttpSession session) {
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByName(email);
		user.setPassword(passwordEncoder.encode(password));
		this.userRepository.save(user);
		
		
		return "redirect:/signin?change=Password changed Successfully";
	}
}
