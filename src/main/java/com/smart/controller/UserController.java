package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

import com.razorpay.*;


@Controller
@RequestMapping("/user")
@CrossOrigin(origins="*")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	/*
	 * Method to add user in all the below pages so that we can display name of logged in user and perform operations accordingly
	 */
	@ModelAttribute
	public void addCommonData(Model m,Principal principal) {
		String userName = principal.getName();
		//fetch user from database
		User user = userRepository.getUserByName(userName);
		m.addAttribute("user", user);
	}
	
	
	@RequestMapping("/dashboard")
	public String dashboard(Model m,Principal principal) {
		/*
		 * String userName = principal.getName(); //fetch user from database User user =
		 * userRepository.getUserByName(userName); m.addAttribute("user", user);
		 * above is task now done by commonData method
		 */
		m.addAttribute("title","Dashboard");
		return "normal/user_dashboard";
	}
	
	/*
	 * add contact form handler
	 * here we send an empty contact object so that spring can automatically create object as per form filled
	 */
	@RequestMapping("/addContact")
	public String addContactForm(Model m) {
		m.addAttribute("title", "Add Contact");	
		m.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	/*
	 * Processing add contact form data
	 * 
	 */
	@PostMapping("/process-contact")
	public String saveContact(
			@ModelAttribute("contact")Contact contact,
			Principal principal,
			@RequestParam("profileImage") MultipartFile file,
			HttpSession session
			) {
		try {
			if(file.isEmpty()) {
				contact.setImage("contact.png");
			}
			else {
				//sets file name in contact
				contact.setImage(file.getOriginalFilename());
				//helps to get a file at location static/image
				File saveFile = new ClassPathResource("static/image").getFile();
				//gives us the path of above file
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				//copies content of input file to file to be saved
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			
			//what are we trying to do is fetch user by help of spring security and then get user by name using userRepository and then we can simply update the contacts the of user and then save new user,contacts will be saved automatically
			String name = principal.getName();
			User user = userRepository.getUserByName(name);
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("Contact Saved");
			//success message
			session.setAttribute("msg",new Message("Successfully saved","success"));
			

		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Error"+e.getMessage());
			//error message
			session.setAttribute("msg",new Message("Oops Something Went wrong!!","danger"));
		}
		return "normal/add_contact_form";
	}
	
	/*
	 * Show all contacts
	 * We are also using concept of pagination
	 * We are showing 5 contacts per pages (n)
	 * And we also want to get user's current page[page]
	 * 
	 */
	@GetMapping("/show-contacts/{page}")
	public String showContacts(
			@PathVariable("page") Integer page,
			Model m,
			Principal principal) {
		m.addAttribute("title", "All contacts");
		//we can fetch the contact using below method, we simply fetch the logged in user from principal and then just use userRepository to fetch all contacts of user
		/*
		String name = principal.getName();
		User user = this.userRepository.getUserByName(name);
		List<Contact> contacts = user.getContacts();
		*/
		//using contact repository to find all contactss
		User user = this.userRepository.getUserByName(principal.getName());
		//here page is current page number, and 5 is number of contacts per page
		PageRequest  pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		//adding current page number
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	/*
	 * show details of a particular contact
	 */
	@GetMapping("/{cId}/contact")
	public String showContactDetail(
			@PathVariable("cId") Integer cid,
			Model m,
			Principal principal
			) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		User user = this.userRepository.getUserByName(principal.getName());
		if(contact.getUser().getId()==user.getId()) {
			m.addAttribute("title",contact.getName());
			m.addAttribute("contact", contact);
		}
		
		
		return "normal/contact_detail";
	}
	/*
	 * Deleting single contact
	 */
	@GetMapping("/delete/{cid}")
	public String deleteContact(
			@PathVariable("cid") Integer cid,
			Model m,
			Principal principal,
			HttpSession s
			) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		User user = this.userRepository.getUserByName(principal.getName());
		if(contact.getUser().getId() == user.getId()) {
			contact.setUser(null);		//unlinking user so that deletion can be performed
			this.contactRepository.delete(contact);
			s.setAttribute("msg", new Message("Deleted Sucessfully!!","success"));
		}
		else {
			s.setAttribute("msg", new Message("Unauthorized to delete this contact","danger"));
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	/*
	 * Open update contact
	 */
	@PostMapping("/update-contact/{cid}")
	public String updateForm(Model m,
			@PathVariable("cid") Integer cid
			) {
		m.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	/*
	 * It will handle update form data
	 */
	@PostMapping("/process-update")
	public String updateContact(
			@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage")MultipartFile file,
			Principal principal,
			Model m,
			HttpSession s
			) {
		try {
			Contact oldContactDetails = this.contactRepository.findById(contact.getCid()).get();
			if(!file.isEmpty()) {
				if(!oldContactDetails.getImage().equals("contact.png")) {
					File deleteFile = new ClassPathResource("static/image").getFile();
					File file1 = new File(deleteFile,oldContactDetails.getImage());
					file1.delete();
				}
				contact.setImage(file.getOriginalFilename());
				//helps to get a file at location static/image
				File saveFile = new ClassPathResource("static/image").getFile();
				//gives us the path of above file
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				//copies content of input file to file to be saved
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			else {
				contact.setImage(oldContactDetails.getImage());
			}
			User user = this.userRepository.getUserByName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			s.setAttribute("msg", new Message("Updated Successfully","success"));
			
		}
					
		catch(Exception e) {
			e.printStackTrace();
			s.setAttribute("msg", new Message("Something Went wrong","danger"));
		}
		System.out.println("Name" +contact);
		System.out.println("CID" + contact.getCid());
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	/*
	 * Your profile handler
	 */
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		m.addAttribute("title", "Profile");
		return "normal/profile";
	}
	
	/*
	 * It opens setting page
	 */
	@GetMapping("/settings")
	public String openSetting(Model m) {
		m.addAttribute("title","Settings");
		return "normal/settings";
	}
	
	/*
	 *  here we will change the password by taking input old and new password in settings page
	 */
	@PostMapping("/change-password")
	public String changePassword(
			@RequestParam("oldPassword")String oldPassword,
			@RequestParam("newPassword") String newPassword,
			Principal principal,
			HttpSession s) {
		System.out.println(oldPassword + " "+ newPassword);
		User user = this.userRepository.getUserByName(principal.getName());
		
		if(this.passwordEncoder.matches(oldPassword, user.getPassword())) {
			
			user.setPassword(this.passwordEncoder.encode(newPassword));
			this.userRepository.save(user);
			s.setAttribute("msg", new Message("Password Changed Successfully","success"));
		}
		else {
			s.setAttribute("msg",new Message("Something Went Wrong !!","danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/dashboard";
	}
	
	/*
	 * Payment Gateway Order ID Creation handler
	 * This method handles the request coming for generation of order ID
	 */
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object> data,Principal principal) throws RazorpayException {
		System.out.println(data);
		int amt = Integer.parseInt(data.get("amount").toString());
		RazorpayClient razorpayClient = new RazorpayClient("rzp_test_sxKQjOHWhw2WeY","YH277wviljYIXV68Bykd8Amw");
		JSONObject ob = new JSONObject();
		ob.put("amount", amt*100);			//conver rupees into paisa
		ob.put("currency", "INR");
		ob.put("receipt","txn_13542");
		
		//creating new order
		Order order = razorpayClient.orders.create(ob);
        System.out.println(order);
        //if you want you can save that order info in database
        MyOrder myOrder = new MyOrder();
        myOrder.setAmount(order.get("amount").toString());
        myOrder.setOrderId(order.get("id"));
        myOrder.setPaymentId(order.get(null));
        myOrder.setStatus(order.get("created"));
        User user = this.userRepository.getUserByName(principal.getName());
        myOrder.setUser(user);
        myOrder.setReceipt(order.get("receipt"));
        this.myOrderRepository.save(myOrder);
        //sending order to client
		return order.toString();
	}
	
	@PostMapping("/update_order")
	@ResponseBody
	public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object> data) {
		System.out.println(data);
		MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		this.myOrderRepository.save(myOrder);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
	
	
}
