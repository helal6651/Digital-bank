package com.user_service.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

@Data
public class PageableResponseDTO<T> implements Serializable {
	private static final long serialVersionUID = 2644975981508L;

	private Long total = 0L;
	private Integer totalPage = 0;
	private Collection<T> data = new HashSet<>();
}