package com.ptms.dto; import jakarta.validation.constraints.NotBlank; public record TeamDto(Long id,@NotBlank String name,String department,String description,boolean enabled){}
