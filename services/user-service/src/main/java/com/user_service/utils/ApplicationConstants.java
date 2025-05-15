package com.user_service.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ApplicationConstants {

	private ApplicationConstants() {
	}
	public static final String AUTHENTICATION_HEADER_NAME = "Authorization";

	public static final String ROLE_PREFIX = "ROLE_";
	public static final String VALID_EMAIL_ADDRESS_REGEX = "^.+@[^-\\.].*\\.[a-z]{2,}$";
	public static final String VALID_PHONE_REGEX = "\\d{8,20}";
	//public static final String VALID_PASSWORD_REGEX = "(?=^.{8,}$)(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s)[0-9a-zA-Z!@#$%^&*()]*$";
	public static final String VALID_PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s)[0-9a-zA-Z!@#$%^&*()]{8,}$";
	public static final Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	public static final String DATA = "data";
	public static final String TOKEN_TYPE = "token_type";
	public static final String REQUEST_ID = "requestId";
	public static final String PRIVATE_KEY = "private_key";
	public static final String PUBLIC_KEY = "public_key";
	public static final String VERSION = "version";
	public static final String RSA_KEY_VERSION = "ras_key_version";
	public static final String USER_ADD = "user:add";
	public static final String TOKEN_RENEW = "token:renew";
	public static final Long ZERO = 0L;
	public static final Integer ONE = 1;
	public static final Long ONE_LONG = 1L;
	public static final Integer TWENTY = 20;
	public static final Long DEFAULT_OFFSET = 10L;
	public static final Long MAXIMUM_OFFSET = 1000L;

	public static final Double ZERO_DOUBLE_VALUE = 0.0;

	public static final String REGEX_ALL = " * ";
	public static final String ALL = "*";

	public static final long ONE_DAY_IN_MILLI = 24L * 3600 * 1000;
	public static final long ONE_HOUR_IN_MILLIS = 3600L * 1000;


	public static final int VALUE_ZERO = 0;
	public static final int VALUE_ONE = 1;
	public static final int VALUE_NEGATIVE_ONE = -1;

	public static final String LANGUAGE = "lan";
	public static final String ACCEPT_LANGUAGE = "Accept-Language";
	public static final String ENGLISH_LANG = "en";
	public static final String JAPANESE_LANGUAGE = "ja";

	public final class StringUtils {
		private StringUtils() {
		}

		public static final String COMMA = ",";
		public static final String COLON = ":";
		public static final String EQUAL = "=";
		public static final String SEMI_COLON = ";";
		public static final String LESS_EQUAL = "<=";
		public static final String BACK_SLASH = "/";
		public static final String HYPHEN = "-";
		public static final String UNDER_SCORE = "_";
		public static final String WHITESPACE_SEQUENCE = "\\s+";
		public static final String ASTERISK = "*";
		public static final String EMPTY_STRING = "";
		public static final String DOT = ".";
	}

	public final class SortOrder {
		private SortOrder() {
		}

		public static final String ASC = " asc";
		public static final String DESC = " desc";
	}

	public static final String OK_MSG = "OK";
	public static final String SUCCESS_CODE = "200";
	public static final String CREATED_SUCCESS_CODE = "201";
	public static final String CREATED_MSG = "Created";
	public static final String UPDATED_MSG = "Updated";

	public static final String SUCCESS = "Success";

	public final class PasswordLength {
		public static final int MIN_LENGTH = 8;
		public static final int MAX_LENGTH = 32;
	}

}
