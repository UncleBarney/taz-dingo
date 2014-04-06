package edu.neu.device;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tazdingo.core.util.ConstantUtil;

import edu.neu.coe.platform.device.platform.device.ConfigDevice;
import edu.neu.coe.platform.device.platform.device.IDevice;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	private static IDevice device=(new ConfigDevice()).defaultDeviceConfiguration();
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		String response=device.userLogin("user1", "123");
		if(response.equals(ConstantUtil.SUCCESS_LOGIN))
		response=device.userAuthorization();
		if(response.equals(ConstantUtil.SUCCESS_GET_SERVICE_TICKET))
			response=device.sendGeneralServiceRequest("service", new HashMap<String,String>()).get("error");
		System.out.println(response);
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}
	
}
