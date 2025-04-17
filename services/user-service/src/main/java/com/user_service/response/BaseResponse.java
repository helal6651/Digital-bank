package com.user_service.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.user_service.enums.ResponseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1546524853123L;

	@JsonProperty("type")
	private ResponseType responseType;

	@JsonProperty("message")
	private Collection<String> message;

	@JsonProperty("result")
	private Object result;

	@JsonProperty("error")
	private Object error;

	@JsonProperty("code")
	private String code;
}
