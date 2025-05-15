package com.user_service.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Getter
@Setter
@PropertySource("classpath:messages-en.properties")
public class Messages {
	@Value("${user.exists}")
	private String userExists;

	@Value("${user.not.exists}")
	private String userNotExists;

	@Value("${password.change.success}")
	private String passwordUpdate;

	@Value("${email.sent.forgot.password}")
	private String forgotPasswordMailSent;

	@Value("${email.sent}")
	private String emailSent;

	@Value("${token.invalid}")
	private String invalidToken;

	@Value("${token.missed}")
	private String missedToken;

	@Value("${token.expired}")
	private String expiredToken;

	@Value("${email.sent.invite-user}")
	private String invitationMailSent;

	@Value("${user.registration.success}")
	private String registrationSuccess;

	@Value("${email.notification.settings.successful}")
	private String successfulEmailNotificationSettings;

	@Value("${email.notification.settings.failed}")
	private String failedEmailNotificationSettings;

	@Value("${notification.email.add}")
	private String addNotificationEmail;

	@Value("${notification.email.update}")
	private String updateNotificationEmail;

	@Value("${notification.email.delete}")
	private String deleteNOtificationEmail;

	@Value("${company.profile.added}")
	private String addCompanyProfile;

	@Value("${company.profile.updated}")
	private String updateCompanyProfile;

	@Value("${tag.name.add}")
	private String addTagName;

	@Value("${tag.name.update}")
	private String updateTagName;

	@Value("${tag.delete}")
	private String deleteTag;

	@Value("${update.successful}")
	private String updateSuccessful;

	@Value("${company.page.status}")
	private String companyLPStatus;

	@Value("${file.upload.success}")
	private String fileUploadSuccess;

	@Value("${no.products.found}")
	private String noProductsFound;

	@Value("${publish.request.sent}")
	private String displayRequestSent;

	@Value("${publish.request.cancelled}")
	private String displayRequestCancelled;

	@Value("${publish.request.updated}")
	private String publishRequestUpdated;

	@Value("${movie.connected.success}")
	private String movieConnected;

	@Value("${invalid.publish.request.type}")
	private String invalidPublishRequestType;

	@Value("${request.successfully.accepted}")
	private String publishRequestAccepted;

	@Value("${request.successfully.rejected}")
	private String publishRequestRejected;

	@Value("${activation.content.update.successful}")
	private String activationContentUpdateSuccessful;

	@Value("${goods.activation.active}")
	private String goodsActivated;

	@Value("${goods.activation.inactive}")
	private String goodsInactivated;

	@Value("${goods.activation.already.active}")
	private  String goodsAlreadyActivated;

	@Value("${goods.activation.already.inactive}")
	private  String goodsAlreadyInactivated;

	@Value("${assign.content.add}")
	private String addAssignContent;

	@Value("${assign.content.update}")
	private String updateAssignContent;

	@Value("${invalid.authentication.type}")
	private  String invalidAuthenticationType;

	@Value("${address.save.success}")
	private  String addressSavedSuccessful;

	@Value("${update.basic.info}")
	private String updateBasicInfo;

	@Value("${update.email}")
	private String updateEmail;

	@Value("${update.password}")
	private String updatePassword;

	@Value("${update.address}")
	private String updateAddress;

	@Value("${toggle.sns}")
	private String toggleSNS;

	@Value("${user.logout}")
	private String logoutSuccess;

	@Value("${redis.clean}")
	private String redisCacheClean;
}