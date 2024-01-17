package com.smart.helper;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Component("sessionAttributeRemover")
public class SessionAttributeRemover {
	
	public void removeSessionAttribute() {
		try {
			HttpSession session = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getSession();
			session.removeAttribute("msg");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	/*
	 * public void removeAnyAttributeFromSession(String attri) { try { HttpSession
	 * session =
	 * ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).
	 * getRequest().getSession(); session.removeAttribute(attri); } catch(Exception
	 * e) { e.printStackTrace(); }
	 * 
	 * }
	 */
}
