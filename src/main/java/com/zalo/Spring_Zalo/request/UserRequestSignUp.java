package com.zalo.Spring_Zalo.request;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestSignUp implements Serializable {
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private String businessName;
    @Override
    public String toString() {
        return "UserRequestSignUp [username=" + username + ", email=" + email + ", password=" + password
                + ", confirmPassword=" + confirmPassword + ", businessName=" + businessName + "]";
    }
    
    
   
}
