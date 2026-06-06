package com.vex.owl.notification.api.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailTemplateUpdateRequest {

    @Size(max = 100)
    private String name;

    private String subject;

    private String content;

    private String remark;

    private Boolean enabled;
}