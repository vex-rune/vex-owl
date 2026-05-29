package com.vex.owl.user.user.auth.domain.profile.entity;

import com.vex.queries.jpa.model.JpaBasicEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "user_social_account", indexes = {
        @Index(name = "idx_social_platform", columnList = "platform"),
        @Index(name = "idx_social_openid", columnList = "open_id")
})
public class SocialAccountEntity extends JpaBasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfileEntity userProfile;

    @Column(nullable = false, length = 50)
    private String platform;

    @Column(nullable = false, length = 255)
    private String account;

    @Column(length = 255)
    private String openId;

    @Column(columnDefinition = "TEXT")
    private String extraInfo;

    public void setExtraInfoFromMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            this.extraInfo = null;
            return;
        }
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> sb.append(k).append("=").append(v).append(";"));
        this.extraInfo = sb.toString();
    }

    public Map<String, String> getExtraInfoAsMap() {
        if (extraInfo == null || extraInfo.isBlank()) {
            return Map.of();
        }
        Map<String, String> result = new java.util.HashMap<>();
        for (String pair : extraInfo.split(";")) {
            if (pair.contains("=")) {
                String[] parts = pair.split("=", 2);
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }
}