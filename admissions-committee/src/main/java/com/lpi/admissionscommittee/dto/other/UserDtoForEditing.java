package com.lpi.admissionscommittee.dto.other;

import com.lpi.admissionscommittee.annotations.PasswordMatcher;
import com.lpi.admissionscommittee.annotations.ValidEmail;
import com.lpi.admissionscommittee.annotations.ValidPassword;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringExclude;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@PasswordMatcher
public class UserDtoForEditing {

    @EqualsAndHashCode.Exclude
    @Setter(AccessLevel.NONE)
    private Long id;

    @Setter(AccessLevel.NONE)
    private String username;

    @ValidEmail
    private String email;

    @ValidPassword(message = "{validation.password.not.blank}")
    @ToStringExclude
    private String password;

    @ValidPassword(message = "{validation.password.repeat}")
    @ToStringExclude
    private String matchingPassword;
}
