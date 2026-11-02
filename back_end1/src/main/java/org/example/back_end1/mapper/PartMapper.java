package org.example.back_end1.mapper;

import org.example.back_end1.dto.CreatePartRequest;
import org.example.back_end1.dto.PartDto;
import org.example.back_end1.entity.Part;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PartMapper {
    PartDto toDto(Part entity);
    List<PartDto> toDtoList(List<Part> entities);
    Part toEntity(CreatePartRequest request);
    void updateEntityFromDto(CreatePartRequest request, @MappingTarget Part entity);
}