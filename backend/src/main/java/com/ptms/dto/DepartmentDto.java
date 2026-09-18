package com.ptms.dto; import jakarta.validation.constraints.NotBlank; public record DepartmentDto(Long id,@NotBlank String name,String description,boolean enabled){}
