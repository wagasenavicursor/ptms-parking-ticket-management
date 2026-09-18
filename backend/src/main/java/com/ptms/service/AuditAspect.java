package com.ptms.service;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import jakarta.servlet.http.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;
import java.lang.reflect.Array;
import java.util.*;

@Aspect
@Component
public class AuditAspect {
  private final AuditService audit;
  private final ObjectMapper mapper;

  public AuditAspect(AuditService audit,ObjectMapper mapper){this.audit=audit;this.mapper=mapper;}

  @Around("execution(* com.ptms.controller..*(..)) && (@annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.PutMapping) || @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
  public Object capture(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request=currentRequest();
    String uri=request==null?"":request.getRequestURI();
    if(skip(uri)) return joinPoint.proceed();

    Object result=joinPoint.proceed();
    try{
      HttpSession session=request==null?null:request.getSession(false);
      String username=session==null?null:(String)session.getAttribute("username");
      String role=session==null?null:(String)session.getAttribute("role");
      if(username==null||username.isBlank()) return result;

      String action=action(request);
      String entity=entityType(uri);
      String entityId=entityId(uri,result);
      String summary=action+" "+entity+(entityId==null?"":" #"+entityId);
      String details=details(joinPoint.getArgs(),result,uri);
      audit.record(username,role,action,entity,entityId,summary,details);
    }catch(Exception ignored){
      // Audit logging must never break the successful business operation.
    }
    return result;
  }

  private HttpServletRequest currentRequest(){
    RequestAttributes a=RequestContextHolder.getRequestAttributes();
    return a instanceof ServletRequestAttributes s?s.getRequest():null;
  }

  private boolean skip(String uri){
    return uri.contains("/api/auth/login")||uri.contains("/api/auth/logout")||
      uri.contains("/api/auth/forgot-password")||uri.contains("/api/auth/reset-password");
  }

  private String action(HttpServletRequest r){
    String uri=r==null?"":r.getRequestURI();
    String method=r==null?"":r.getMethod();
    if(uri.endsWith("/complete"))return "COMPLETE";
    if(uri.endsWith("/cancel"))return "CANCEL";
    if(uri.endsWith("/bulk-scan"))return "BULK_CREATE";
    if(uri.contains("/change-password"))return "PASSWORD_CHANGE";
    return switch(method){case "POST"->"CREATE";case "PUT","PATCH"->"UPDATE";case "DELETE"->"DELETE";default->method;};
  }

  private String entityType(String uri){
    String path=uri.replaceFirst("^.*?/api/","");
    String root=path.split("/")[0];
    return switch(root){
      case "employees"->"Employee";
      case "visitors"->"Visitor";
      case "departments"->"Department";
      case "teams"->"Team";
      case "tickets"->"Parking Ticket";
      case "issues"->"Ticket Issue";
      case "reconciliations"->"Reconciliation";
      case "settings"->"Settings";
      case "users"->"User";
      case "auth"->"Account";
      default->root.isBlank()?"Record":root;
    };
  }

  private String entityId(String uri,Object result){
    String[] parts=uri.split("/");
    for(int i=0;i<parts.length;i++){
      if("api".equals(parts[i])&&i+2<parts.length&&parts[i+2].matches("\\d+"))return parts[i+2];
    }
    try{
      JsonNode n=mapper.valueToTree(result);
      if(n!=null&&n.isObject()&&n.hasNonNull("id"))return n.get("id").asText();
    }catch(Exception ignored){}
    return null;
  }

  private String details(Object[] args,Object result,String uri){
    ObjectNode root=mapper.createObjectNode();
    ArrayNode requestValues=root.putArray("request");
    for(Object arg:args){
      if(arg==null||arg instanceof HttpSession||arg instanceof HttpServletRequest||arg instanceof HttpServletResponse)continue;
      requestValues.add(sanitize(mapper.valueToTree(arg)));
    }
    if(!uri.contains("/change-password")&&result!=null)root.set("result",sanitize(mapper.valueToTree(result)));
    try{return mapper.writeValueAsString(root);}catch(Exception e){return null;}
  }

  private JsonNode sanitize(JsonNode node){
    if(node==null)return NullNode.getInstance();
    if(node.isObject()){
      ObjectNode out=mapper.createObjectNode();
      node.fields().forEachRemaining(e->{
        String k=e.getKey();
        String low=k.toLowerCase(Locale.ROOT);
        if(low.contains("password")||low.contains("token")||low.contains("profileimage")||low.contains("passwordhash"))out.put(k,"[REDACTED]");
        else out.set(k,sanitize(e.getValue()));
      });
      return out;
    }
    if(node.isArray()){
      ArrayNode out=mapper.createArrayNode();
      node.forEach(v->out.add(sanitize(v)));
      return out;
    }
    return node;
  }
}
