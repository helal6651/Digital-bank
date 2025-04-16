package com.user_service.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;

public class ApplicationConstants {

	private ApplicationConstants() {
	}

	public static final String ROLE_PREFIX = "ROLE_";
	public static final String VALID_EMAIL_ADDRESS_REGEX = "^.+@[^-\\.].*\\.[a-z]{2,}$";
	public static final String VALID_PHONE_REGEX = "\\d{8,20}";
	public static final String VALID_PASSWORD_REGEX = "(?=^.{8,}$)(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s)[0-9a-zA-Z!@#$%^&*()]*$";
	public static final String VALID_IMAGE_URL_REGEX = "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)";
	public static final String VALID_VIDEO_URL_REGEX = "^.*\\.(avi|AVI|wmv|WMV|flv|FLV|mpg|MPG|mp4|MP4)$";
	public static final String VALID_LP_PAGE_NAME = "^[a-zA-Z0-9_-]+( [a-zA-Z0-9_-]+)*$";
	public static final String VALID_COMPANY_NAME = "^[a-zA-Z0-9_-]+( [a-zA-Z0-9_-]+)*$";

	public static final String REQUEST_ID = "requestId";
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

	public final class MailType {
		public static final int CMS_RESET_PASSWORD = 1;
		public static final int CMS_INVITE_USER = 2;
		public static final int WMC_ACTIVE_USER = 3;
		public static final int WMC_RESET_PASSWORD = 4;
		public static final int WMC_POST_INQUIRY_ADMIN = 5;
		public static final int PASSWORD_RESET_SUCCESS = 6;
		public static final int WMC_POST_INQUIRY = 7;
		public static final int CMS_GOODS_PUBLISH_REQUEST = 8;
		public static final int CMS_GOODS_PUBLISH_REQUEST_ACCEPTED_REJECTED = 9;
		public static final int CMS_CONTENTS_BORROW_REQUEST = 10;
		public static final int CMS_CONTENTS_BORROW_REQUEST_ACCEPTED_REJECTED = 11;
		public static final int CMS_GOODS_PUBLISH_REQUEST_ACTIVATED = 12;
		public static final int CMS_GOODS_PUBLISH_REQUEST_DEACTIVATED = 13;
		public static final int CMS_CONTENTS_PUBLISH_REQUEST_CANCELLED = 14;
		public static final int CMS_GOODS_PUBLISH_REQUEST_CANCELLED = 15;
		public static final int CMS_AFFILIATE_MOVIES_PUBLISH_REQUEST = 16;
		public static final int WMC_CONFIRM_ORDER = 17;

	}

	public static final HashMap<Integer, String> mailSubject = new HashMap<Integer, String>() {{
		put(MailType.CMS_RESET_PASSWORD, "cms.forgot.password.email.subject");
		put(MailType.WMC_RESET_PASSWORD, "forgotpassword.email.subject");
		put(MailType.CMS_INVITE_USER, "registration.email.subject");
		put(MailType.WMC_ACTIVE_USER, "wmc.registration.email.subject");
		put(MailType.WMC_POST_INQUIRY, "post.inquiry.email.subject");
		put(MailType.WMC_POST_INQUIRY_ADMIN, "post.inquiry.super.admin.email.subject");
		put(MailType.PASSWORD_RESET_SUCCESS, "password.reset.email.subject");
		put(MailType.CMS_GOODS_PUBLISH_REQUEST, "cms.goods.publish.request.email.subject");
		put(MailType.CMS_GOODS_PUBLISH_REQUEST_ACCEPTED_REJECTED, "cms.goods.publish.request.accepted.rejected.email.subject");
		put(MailType.CMS_CONTENTS_BORROW_REQUEST, "cms.contents.borrow.request.email.subject");
		put(MailType.CMS_CONTENTS_BORROW_REQUEST_ACCEPTED_REJECTED, "cms.contents.borrow.request.accepted.rejected.email.subject");
		put(MailType.CMS_GOODS_PUBLISH_REQUEST_ACTIVATED, "cms.goods.publish.request.activated.email.subject");
		put(MailType.CMS_GOODS_PUBLISH_REQUEST_DEACTIVATED, "cms.goods.publish.request.deactivated.email.subject");
		put(MailType.CMS_CONTENTS_PUBLISH_REQUEST_CANCELLED, "cms.contents.borrow.request.cancelled.email.subject");
		put(MailType.CMS_GOODS_PUBLISH_REQUEST_CANCELLED, "cms.goods.publish.request.cancelled.email.subject");
		put(MailType.CMS_AFFILIATE_MOVIES_PUBLISH_REQUEST, "cms.affiliate.movies.publish.request.email.subject");
		put(MailType.WMC_CONFIRM_ORDER, "wmc.order.confirm.email.subject");
	}};

	public final class MailTemplateFields {
		public static final String LINK = "link";
		public static final String SUBJECT = "subject";
		public static final String CODE = "code";
		public static final String USER_NAME = "userName";
		public static final String EMAIL_DOMAIN = "emailDomain";
		public static final String SERVICE_NAME = "serviceName";
		public static final String ROOT_URL = "rootUrl";
		public static final String CONTACT_URL = "contactUrl";
	}

	public final class InquiryMailTemplateFields {
		public static final String NAME = "name";
		public static final String CONTENT = "content";
		public static final String INQUIRY_TYPE = "inquiryType";
		public static final String SUPER_ADMIN = "superAdminName";
	}

	public static final String EMAIL_URL_TOKEN_PARAMETER = "?token=";
	public static final String EMAIL_URL_EMAIL_PARAMETER = "&email=";
	public static final String EMAIL_URL_ROLE_PARAMETER = "&role=";

	public static final String TEMP_DIR = "/tmp";

	public static final String APPLICATION_LOCAL_PROFILE = "local";
	public static final String APPLICATION_DEV_PROFILE = "dev";
	public static final Integer CANCEL_POLICY = 0;
	public static final Integer DELAY_POLICY = 1;

	public static final String SUPER_ADMIN_USER_ID = "SUPER_ADMIN_USER_ID";

	public static final String SUCCESS = "Success";

	public final class ContentType {
		public static final int GOODS = 1;
		public static final int COORDINATES = 2;
	}

	public final class NotificationType {
		public static final int GOODS = 1;
		public static final int COORDINATES = 2;
		public static final int MOVIE = 3;
	}

	public final class DisplayType {
		public static final int RENTAL = 1;
		public static final int PURCHASE = 2;
	}

	public final class PasswordLength {
		public static final int MIN_LENGTH = 8;
		public static final int MAX_LENGTH = 32;
	}

	public static final Date EPOCH_START_DATE = new Date(1970, 0, 1);
	public static final String VIDEO_MP4 = "video/mp4";

	public final class VerificationTokenType {
		public static final int ADMIN_INVITATION = 1;
		public static final int RESET_PASSWORD = 2;
		public static final int OTP = 3;
	}

	public static final Integer NEGATIVE_ONE = -1;

	public static final double VALUE_HUNDRED = 100.00;
	public static final String AFFILIATE_LP_TAGS_SEPARATOR = "\\;";
	public static final String DUPLICATE_ENTRY = "Duplicate entry";

	public final class Resources {
		public static final String LOGO = "logo";
		public static final String FAV_ICON = "favIcon";
		public static final String PAY_JP_KEY = "payJPKey";
	}

	public final class EmailSubject {
		public static final String PUBLISH_REQUEST = "Publish request";
		public static final String BORROW_REQUEST = "Borrow request";
	}

	public static String payJPKey = "pk_test_a5ec1d4c799d4c91635caef9";
	public static Integer INITIAL_PAGE_SIZE = 0;
	public static Integer RELATED_CONTENTS_MAX_LIMIT = 4;
	public static final Integer IMAGE_FILE_SIZE_BYTE = 10_00_000;

	public static final ZonedDateTime CURRENT_ZDT_UTC = ZonedDateTime.now(ZoneId.of("UTC"));

    public static ZonedDateTime getCurrentTime() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

	public static final int MAX_ATTEMPT = 2;
	public static final long ONE_HOUR_IN_SECONDS = 3600L;
	public static final String DASSEN_API_ACCESS_KEY = "http-x-dassen-api-key";
	public static final String ONLY_DIGIT = "\\d+";
	public static final String PACKAGE_TOKEN_ACCESS_ERROR = "You don't have permission to access";
}
