package com.user_service.validators;

import com.user_service.utils.ApplicationConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

	private ValidPassword validPassword;

	@Override
	public void initialize(ValidPassword validPassword) {
		this.validPassword = validPassword;
	}


	@Override
	public boolean isValid(String password, ConstraintValidatorContext context) {
		if (StringUtils.isEmpty(password)) return false;

		if(!isValidPasswordLength(password)) return false;

		if (isPasswordValid(password))
			return true;
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(
				validPassword.message()).addConstraintViolation();
		return false;
	}

	public static boolean isValidPasswordLength(String password){
		return password.length()>= ApplicationConstants.PasswordLength.MIN_LENGTH
				&& password.length()<=ApplicationConstants.PasswordLength.MAX_LENGTH;
	}

	private boolean isPasswordValid(String password) {
		Pattern pattern = Pattern.compile(ApplicationConstants.VALID_PASSWORD_REGEX);
		Matcher matcher = pattern.matcher(password);
		return matcher.find();
	}
}
