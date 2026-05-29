package com.vex.owl.user.user.auth.domain.profile.entity;

import com.vex.queries.jpa.id.BizIdPrefix;
import com.vex.queries.jpa.id.BizSnowId;
import com.vex.queries.jpa.model.JpaBasicWithIdEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@BizIdPrefix(value = "USR")
@Table(name = "user_profile", indexes = {
        @Index(name = "idx_user_nickname", columnList = "nickname")
})
public class UserProfileEntity extends JpaBasicWithIdEntity {

    @Id
    @BizSnowId
    private String id;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Column(length = 255)
    private String email;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SocialAccountEntity> socialAccounts = new ArrayList<>();

    public void addSocialAccount(SocialAccountEntity socialAccount) {
        socialAccounts.add(socialAccount);
        socialAccount.setUserProfile(this);
    }

    public void removeSocialAccount(SocialAccountEntity socialAccount) {
        socialAccounts.remove(socialAccount);
        socialAccount.setUserProfile(null);
    }

    public void clearSocialAccounts() {
        socialAccounts.forEach(sa -> sa.setUserProfile(null));
        socialAccounts.clear();
    }
}