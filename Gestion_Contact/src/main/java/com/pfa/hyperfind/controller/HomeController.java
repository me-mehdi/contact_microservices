package com.pfa.hyperfind.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.WriterException;
import com.pfa.hyperfind.dao.ContactRepository;
import com.pfa.hyperfind.dao.ContactStorageService;
import com.pfa.hyperfind.dao.UserRepository;
import com.pfa.hyperfind.dao.UserStorageService;
import com.pfa.hyperfind.entity.Contact;
import com.pfa.hyperfind.entity.ContactDetails;
import com.pfa.hyperfind.entity.User;
import com.pfa.hyperfind.qr.QRCodeGenerator;
import com.pfa.hyperfind.qr.QRCodeGeneratorr;



@Controller
public class HomeController {
	
//	public static String uploadDirectory = System.getProperty("user.dir")+"/src/main/resources/static/images";
	private static int currUserId;
	private static User pUser;
	private List<ContactDetails> favourites;
	private List<Contact> contactList;
	private List<Contact> favouriteList;
	
	@Autowired
	private UserStorageService userStorageService;
	
	@Autowired
	private ContactStorageService contactStorageService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository ContactRepository;
	@GetMapping({"/","/home"})
	public String getHome(Model model, HttpSession session, @RequestParam(name = "page",defaultValue = "0") Integer page) throws IOException {
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String currUsername = "";
		
		if (principal instanceof UserDetails) {
		  currUsername = ((UserDetails)principal).getUsername();
		} else {
		  currUsername = principal.toString();
		}
		
		System.out.println("---------------------------> currUser: " + currUsername);
		
		if (!currUsername.equals("anonymousUser")) {
			
			User currUser = userStorageService.findByUsername(currUsername).get();
//			User currUser = docStorageService.findByUsername(currUsername).get();
			
			System.out.println("----------------------------> CurrUser id: " + currUser.getId());
			
			model.addAttribute("currUser", currUser);
			model.addAttribute("id", currUser.getId());
			session.setAttribute("user", currUser);
			currUserId = currUser.getId();
			pUser = currUser;
			
		}
		
		
		// adding pagination
		Pageable pagable = PageRequest.of(page, 4);
		
		System.out.println("\n\n\n\n\n\n===========Pagination sql:");
		System.out.println("Page: " + page + " UId: " + currUserId);
		Page<ContactDetails> contacts = contactStorageService.findByUserid(currUserId, pagable);
		
		
		contactList = new ArrayList<>(); 
		
		for (ContactDetails c : contacts) {
			contactList.add(new Contact(c.getId(), c.getName(), c.getGender(), c.getEmail(), c.getPosition(), c.getPhone(), c.getAddress(), getImgData(c.getImage()), c.getUserid(), c.getFavourite()));
		}
		
		
		System.out.println("\n\n\n\n\n\n===========Pringting found contacts:");
		System.out.println("Page: " + page);
		
		List<ContactDetails> contacts2 = contactStorageService.findByUseridOrderByNameAsc(currUserId);
		model.addAttribute("allContacts", contactList);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		model.addAttribute("totalContacts", contacts2.size());
		
		
		// adding favourites to model
		favourites = contactStorageService.findByFavouriteOrderByNameAsc("1");
		favouriteList = new ArrayList<>();
		for (ContactDetails c : favourites) {
			favouriteList.add(new Contact(c.getId(), c.getName(), c.getGender(), c.getEmail(), c.getPosition(), c.getPhone(), c.getAddress(), getImgData(c.getImage()), c.getUserid(), c.getFavourite()));
		}
		
		model.addAttribute("favourites", favouriteList);
		

		return "home";




	}
	
	
	@GetMapping("/search")
	public String search(@RequestParam(name = "param") String search,
						Model model,
						@RequestParam(name = "page",defaultValue = "0") Integer page) {
		
		System.out.println("\n\nEntered search controller");
		Pageable pageable = PageRequest.of(page, 3);
		Page<ContactDetails> searchResult = contactStorageService.search(search, currUserId, pageable);
		System.out.println("\n\n========================>> Search results");
		System.out.println("total pages: " + searchResult.getTotalPages());
		
		
		List<Contact> searchList = new ArrayList<>();
		
		for (ContactDetails c : searchResult)
			searchList.add(new Contact(c.getId(), c.getName(), c.getGender(), c.getEmail(), c.getPosition(), c.getPhone(), c.getAddress(), getImgData(c.getImage()), c.getUserid(), c.getFavourite()));
		model.addAttribute("searchResults", searchList);
		model.addAttribute("favourites", favouriteList);		
		
		System.out.println("\n\n\n\n\n\n===========Pagination sql:");
		System.out.println("Page: " + page + " UId: " + currUserId);
		
		List<ContactDetails> contacts2 = contactStorageService.findByUseridOrderByNameAsc(currUserId);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", searchResult.getTotalPages());
		model.addAttribute("totalContacts", contacts2.size());
		
		
		return "search-results";
	}
	 
	public static String getImgData(byte[] byteData) {
	    return Base64.encodeBase64String(byteData);
	}
	
	@GetMapping("/profile")
	public String getProfile(Model model) {		
		model.addAttribute("profile", pUser);
		model.addAttribute("favourites", favouriteList);
		model.addAttribute("imgUtil", getImgData(pUser.getImage()));
		return "profile";
	}
	
	@PostMapping("/updateProfile")
	public String updateProfile(@ModelAttribute(name = "profile") User user,
			@RequestParam(name = "image12", required = false) MultipartFile multipartFile) throws IOException {
		
		System.out.println("User from view: " + user);
		System.out.println("Retrieved image: " + multipartFile.getOriginalFilename() + " size: " + multipartFile.getSize());
		User retrievedUser = userStorageService.saveUser(multipartFile, user);				
//		userStorageService.saveUser(multipartFile, retrievedUser);		
		System.out.println("\n\nRetrieved User: " + retrievedUser);		
		return "redirect:/home";
	}
	
	@GetMapping("/deleteProfile")
	public String deleteProfile(@RequestParam(name = "param") Integer id) {
		userStorageService.deleteById(id);
		return "redirect:/login";
	}
	
	
	@GetMapping("/deleteContact")
	public String deleteContact(@RequestParam(name = "param") String param) {
		contactStorageService.deleteById(Integer.parseInt(param));
		return "redirect:/home";
	}
	
	@GetMapping("/updateContact")
	public String updateContact(@RequestParam(name = "param") String param, Model model) {
		ContactDetails contact = contactStorageService.findById(Integer.parseInt(param));
		System.out.println("==========> For update: " + contact);
//		model.addAttribute("newContact", new ContactDetails());
		model.addAttribute("updateContact", contact);
		model.addAttribute("favourites", favouriteList);
		return "update-contact";
	}
	@GetMapping("/visit")
	public String visit(@RequestParam(name = "param") String param, Model model) {
		ContactDetails contact = contactStorageService.findById(Integer.parseInt(param));
		System.out.println("==========> For update: " + contact);
//		model.addAttribute("newContact", new ContactDetails());
		model.addAttribute("visit", contact);
		model.addAttribute("imgUtil", getImgData(contact.getImage()));

		return "visit";
	}
	
	
	@GetMapping("/addContact")
	public String showAddContact(Model model) {
		model.addAttribute("newContact", new ContactDetails());
		model.addAttribute("favourites", favouriteList);
		return "add-contact";
	}
	
	
	
	
	@PostMapping("/addContact")
	public String addContact(@ModelAttribute(name = "newContact") ContactDetails newContact,
							@RequestParam(name = "image12", required = false) MultipartFile multipartFile) throws IOException {		
		
		ContactDetails savedUser = contactStorageService.save(newContact, multipartFile,currUserId );	
		System.out.println(newContact);	
				
		return "redirect:/home";
	}
	/*@GetMapping("/getAll")
    public ResponseEntity<List<User>> getUsers() throws IOException, WriterException {
        List<User> users = userService.getUsers();
        if (users.size() != 0){
            for (User user : users){
                QRCodeGenerator.generateQRCode(user);
            }
        }
        return ResponseEntity.ok(userService.getUsers());
    }
    */
                                                              //user
	@GetMapping("/users")
	public String getAllUsersWithQRCode(Model model) throws IOException, WriterException {
	    List<User> users = userRepository.findAll();
	    users.forEach(user -> user.setImage(null)); // set image to null to exclude it from the response
	    
	    if (users.size() != 0) {
	        for (User user : users) {
	            QRCodeGeneratorr.generateQRCode(user);
	        }
	    }   
	    model.addAttribute("users", users);	    
		return "redirect:/home";
	}

	                                                         //contact
	@GetMapping("/contacts")
	public String getAllContactWithQRCode(Model model) throws IOException, WriterException {
	    List<ContactDetails> contacts = ContactRepository.findAll();
	    contacts.forEach(contact -> contact.setImage(null)); // set image to null to exclude it from the response
	    
	    if (contacts.size() != 0) {
	        for (ContactDetails contact : contacts) {
	            QRCodeGenerator.generateQRCode(contact);
	        }
	    }   
	    model.addAttribute("contacts", contacts);	    
		return "redirect:/home";
	    
	}

	@GetMapping("/fav")
	public String yahya(Model model) {
	    favourites = contactStorageService.findByFavouriteOrderByNameAsc("1");
	    favouriteList = new ArrayList<>();
	    for (ContactDetails c : favourites) {
	        favouriteList.add(new Contact(c.getId(), c.getName(), c.getGender(), c.getEmail(), c.getPosition(), c.getPhone(), c.getAddress(), getImgData(c.getImage()), c.getUserid(), c.getFavourite()));
	    }

	    model.addAttribute("favourites", favouriteList);

	    return "fav";
	}

	
}
