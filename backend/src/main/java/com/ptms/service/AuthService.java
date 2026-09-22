package com.ptms.service;

import com.ptms.domain.*;
import com.ptms.exception.*;
import com.ptms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service @Transactional
public class AuthService {
  private final AppUserRepository repository;
  private final NavigationPermissionService navigationPermissions;
  public AuthService(AppUserRepository repository,NavigationPermissionService navigationPermissions){this.repository=repository;this.navigationPermissions=navigationPermissions;}
  public Map<String,Object> login(String username,String password){AppUser u=repository.findByUsernameIgnoreCase(username).orElseThrow(()->new BusinessRuleException("Invalid username or password"));if(!u.isEnabled()||!hash(password).equals(u.getPasswordHash()))throw new BusinessRuleException("Invalid username or password");if(u.getRole()==UserRole.SUPER_USER&&u.isMustChangePassword()&&!hash("admin").equals(u.getPasswordHash())){u.setMustChangePassword(false);repository.save(u);}return view(u);}
  public Map<String,Object> current(String username){AppUser u=repository.findByUsernameIgnoreCase(username).orElseThrow(()->new BusinessRuleException("Session user no longer exists"));if(!u.isEnabled())throw new BusinessRuleException("User account is disabled");return view(u);}
  public List<Map<String,Object>> users(){return repository.findAll().stream().sorted(Comparator.comparing(AppUser::getId)).map(this::view).toList();}
  public Map<String,Object> save(Long id,Map<String,Object> q){
    AppUser u=id==null?new AppUser():repository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found"));
    String fullName=text(q.get("fullName")),username=text(q.get("username")),email=text(q.get("email"));
    if(fullName.isBlank())throw new BusinessRuleException("Full name is required");
    if(username.isBlank())throw new BusinessRuleException("Username is required");
    if(email.isBlank())throw new BusinessRuleException("Email is required");
    repository.findByUsernameIgnoreCase(username).filter(x->id==null||!x.getId().equals(id)).ifPresent(x->{throw new BusinessRuleException("Username already exists");});
    repository.findByEmailIgnoreCase(email).filter(x->id==null||!x.getId().equals(id)).ifPresent(x->{throw new BusinessRuleException("Email already exists");});
    u.setFullName(fullName);u.setUsername(username);u.setEmail(email);
    try{u.setRole(UserRole.valueOf(text(q.get("role"))));}catch(Exception e){throw new BusinessRuleException("Select a valid user role");}
    u.setEnabled(!Boolean.FALSE.equals(q.get("enabled")));
    String password=text(q.get("password")),confirmation=text(q.get("passwordConfirm"));
    if(!password.isBlank()&&!password.equals(confirmation))throw new BusinessRuleException("Password and confirmation do not match");
    if(password.isBlank()&&!confirmation.isBlank())throw new BusinessRuleException("Enter the password as well as its confirmation");
    if(!password.isBlank()){u.setPasswordHash(hash(password));u.setMustChangePassword(id==null);}
    if(id==null&&u.getPasswordHash()==null)throw new BusinessRuleException("Password is required");
    Object permissionValue=q.get("navigationPermissions");
    u.setNavigationPermissions(permissionValue==null?null:String.join(",",navigationPermissions.clean(permissionValue)));
    Object image=q.get("profileImage");if(image!=null){String v=String.valueOf(image);if(!v.isBlank()&&!v.startsWith("data:image/"))throw new BusinessRuleException("Profile picture must be an image");if(v.length()>2_000_000)throw new BusinessRuleException("Profile picture is too large");u.setProfileImage(v.isBlank()?null:v);}
    return view(repository.saveAndFlush(u));
  }
  public void delete(Long id,String currentUsername){AppUser u=repository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found"));if(u.getUsername().equalsIgnoreCase(currentUsername))throw new BusinessRuleException("You cannot delete the account currently signed in");repository.delete(u);}
  public String forgot(String email){AppUser u=repository.findByEmailIgnoreCase(email).orElse(null);if(u!=null){u.setResetToken(UUID.randomUUID().toString());u.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(30));repository.save(u);}return "If the email exists, reset instructions have been created.";}
  public void reset(String token,String password){AppUser u=repository.findByResetToken(token).orElseThrow(()->new BusinessRuleException("Invalid reset token"));if(u.getResetTokenExpiresAt()==null||u.getResetTokenExpiresAt().isBefore(LocalDateTime.now()))throw new BusinessRuleException("Reset token expired");u.setPasswordHash(hash(password));u.setResetToken(null);u.setResetTokenExpiresAt(null);u.setMustChangePassword(false);repository.save(u);}
  public void change(String username,String oldPassword,String newPassword){AppUser u=repository.findByUsernameIgnoreCase(username).orElseThrow();if(!hash(oldPassword).equals(u.getPasswordHash()))throw new BusinessRuleException("Current password is incorrect");u.setPasswordHash(hash(newPassword));u.setMustChangePassword(false);repository.save(u);}
  public void ensureAdmin(){if(repository.findByUsernameIgnoreCase("admin").isEmpty()){AppUser u=new AppUser();u.setFullName("PTMS Super User");u.setUsername("admin");u.setEmail("admin@ptms.local");u.setRole(UserRole.SUPER_USER);u.setEnabled(true);u.setMustChangePassword(true);u.setPasswordHash(hash("admin"));repository.save(u);}}
  private Map<String,Object> view(AppUser u){Map<String,Object>m=new LinkedHashMap<>();m.put("id",u.getId());m.put("fullName",u.getFullName());m.put("username",u.getUsername());m.put("email",u.getEmail());m.put("role",u.getRole());m.put("enabled",u.isEnabled());m.put("mustChangePassword",u.getRole()==UserRole.SUPER_USER?hash("admin").equals(u.getPasswordHash()):u.isMustChangePassword());m.put("profileImage",u.getProfileImage());m.put("navigationPermissions",u.getNavigationPermissions()==null?null:Arrays.stream(u.getNavigationPermissions().split(",")).filter(s->!s.isBlank()).toList());m.put("roleNavigationPermissions",navigationPermissions.forRole(u.getRole().name()));return m;}
  private String text(Object v){return v==null?"":String.valueOf(v).trim();}
  private String hash(String s){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
