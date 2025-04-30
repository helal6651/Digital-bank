package com.user_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SecretDto {
    private Map<String, String> data;
    private MetadataDto metadata;
}
