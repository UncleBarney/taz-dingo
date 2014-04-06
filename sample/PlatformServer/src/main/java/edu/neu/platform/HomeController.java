package edu.neu.platform;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tazdingo.http.IPlatform;
import com.tazdingo.platform.PlatformConfig;

import edu.neu.coe.platform.keyserver.IKeyServer;
import edu.neu.coe.platform.keyserver.KeyServerConfig;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	private static IPlatform platform=(new PlatformConfig()).defaultPlatformConfiguration();
	private static IKeyServer keyserver=(new KeyServerConfig()).defaultKeyServerConfiguration();
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public @ResponseBody void takeRequest(Locale locale,HttpServletRequest request,HttpServletResponse response) {
		
		logger.info("Welcome Platform! The client locale is {}.", locale);
		response=platform.takeRequest(request, response);
		
		
	}
	
	@RequestMapping(value="/addUser",method=RequestMethod.POST)
	public String addUser(@RequestParam String username,@RequestParam String privilege,@RequestParam String keyservername){
		if(keyservername==null || keyservername.isEmpty())
		platform.addUser(username, privilege);
		else platform.addUser(username, privilege, keyservername);
		return "redirect:/";
	}
	
	@RequestMapping(value="/addDevice",method=RequestMethod.POST)
	public String addDevice(@RequestParam String deviceid,@RequestParam String privilege,@RequestParam String keyservername){
		if(keyservername==null || keyservername.isEmpty())
	    platform.addDevice(deviceid, privilege);
	    else platform.addDevice(deviceid, privilege, keyservername);
		return "redirect:/";
	}
	
	@RequestMapping(value="/addService",method=RequestMethod.POST)
	public String addService(@RequestParam String servicename,@RequestParam String privilege,@RequestParam String keyservername,@RequestParam String serviceurl){
		if(keyservername==null || keyservername.isEmpty())
		    platform.addService(servicename, serviceurl, privilege);
		    else platform.addService(servicename, serviceurl, privilege, keyservername);
		return "redirect:/";
	}
	
	@RequestMapping(value="/addKeyServer",method=RequestMethod.POST)
	public String addPlatform(@RequestParam String keyservername,@RequestParam String keyserverurl,@RequestParam String privilege){
		platform.addKeyServer(keyservername, keyserverurl, privilege);
		return "redirect:/";
	}
	
	@RequestMapping(value="/blockDevice",method=RequestMethod.POST)
	public String blockDevice(@RequestParam String deviceid){
		platform.blockDevice(deviceid);
		return "redirect:/";
	}
	
	@RequestMapping(value="/keyserver", method=RequestMethod.POST)
	public @ResponseBody void takeRequest(HttpServletRequest request, HttpServletResponse response) {
		keyserver.takeRequest(request, response);
	}
	
}
