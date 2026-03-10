package com.claude.learn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.identity")
public class EnterpriseIdentityProperties {

    private String issuer;
    private String audience;
    private String usernameClaim = "preferred_username";
    private String userIdClaim = "sub";
    private String deptIdClaim = "dept_id";
    private String rolesClaim = "roles";
    private String dataScopesClaim = "data_scopes";
    private String jwtSecret = "change-enterprise-secret-in-production";

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getUsernameClaim() {
        return usernameClaim;
    }

    public void setUsernameClaim(String usernameClaim) {
        this.usernameClaim = usernameClaim;
    }

    public String getUserIdClaim() {
        return userIdClaim;
    }

    public void setUserIdClaim(String userIdClaim) {
        this.userIdClaim = userIdClaim;
    }

    public String getDeptIdClaim() {
        return deptIdClaim;
    }

    public void setDeptIdClaim(String deptIdClaim) {
        this.deptIdClaim = deptIdClaim;
    }

    public String getRolesClaim() {
        return rolesClaim;
    }

    public void setRolesClaim(String rolesClaim) {
        this.rolesClaim = rolesClaim;
    }

    public String getDataScopesClaim() {
        return dataScopesClaim;
    }

    public void setDataScopesClaim(String dataScopesClaim) {
        this.dataScopesClaim = dataScopesClaim;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
}
