package aforo255.com.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aforo255.com.entity.User;
import aforo255.com.service.IUserService;
@Component
public class AuthenticationSuccessErrorHandler  implements AuthenticationEventPublisher {

	@Autowired
	private IUserService userService ;
	
	private Logger log = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);
	
	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		// TODO Auto-generated method stub
		
	if (authentication.getDetails() instanceof WebAuthenticationDetails) {
		return ;
	}
	
	UserDetails user = (UserDetails) authentication.getPrincipal();
	String mensaje = "Success Login: "+ user.getUsername();
	System.out.print(mensaje);
	
	User users= userService.findByUsername(authentication.getName());
	
	if (users.getAttemp() !=null && users.getAttemp()>0) {
		users.setAttemp(0L);
		userService.update(users);
	}
	
	}

	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		// TODO Auto-generated method stub
		String mensaje = "Error in the Login : "+ exception.getMessage();
		log.error(mensaje);
		
		try {
			StringBuilder errors = new StringBuilder();
			errors.append(mensaje);
			
			User users= userService.findByUsername(authentication.getName());
			if (users.getAttemp()==null) {
				users.setAttemp(0L);
			}
			
			log.info("Attemps now is of : "+users.getAttemp() );
			users.setAttemp(users.getAttemp()+1);
			
			log.info("Attemps before is of : "+users.getAttemp() );
			
			errors.append("- Attemps Login : "+users.getAttemp() );
			
			if(users.getAttemp() >= 3) {
				String errorMaxAttemps = String.format("user %s disabled for  max  Attemps.", users.getUsername());
				log.error(errorMaxAttemps);
				errors.append(" - " + errorMaxAttemps);
				users.setEnabled(false);
			}
			
			userService.update(users);
			
		} catch (Exception e) {
			log.error(String.format("user %s does not exist in the system", authentication.getName()));
		}

	}

}
