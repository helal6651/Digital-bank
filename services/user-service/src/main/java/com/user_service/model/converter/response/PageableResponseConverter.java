package com.user_service.model.converter.response;


import com.user_service.response.PageableResponseDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PageableResponseConverter<S, T> {



    public PageableResponseDTO<T> convert(Page<S> source, Converter<S, T> converter) {
        List<T> dataList = source.get().map(converter::convert)
                .collect(Collectors.toList());
        return make(source,dataList);
    }

    public PageableResponseDTO<T> make(Page<S> pageInformation, Collection<T> data){
        PageableResponseDTO<T> responseDTO = new PageableResponseDTO<>();
        responseDTO.setTotal(pageInformation.getTotalElements());
        responseDTO.setData(data);
        responseDTO.setTotalPage(pageInformation.getTotalPages());
        return responseDTO;
    }
}
