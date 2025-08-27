package core.hackathon02api.auth.entity;

import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.support.StringListJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                  // 게시글 ID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false) // 작성자 ID
    private User author;

    @Column(nullable = false)
    private String title;                             // 제목

    private String category;                          // 카테고리

    @Column(name = "product_name", nullable = false)
    private String productName;                       // 상품명

    @Column(name = "product_url")
    private String productUrl;                        // 상품 URL

    @Lob
    @Column(name = "product_desc")
    private String productDesc;                       // 상품 설명/가격 정보

    @Column(name = "desired_member_count")
    private Integer desiredMemberCount;               // 모집 희망 인원

    @Lob
    private String content;                           // 글 내용

    @Column(name = "main_image_url")
    private String mainImageUrl;                      // 대표 이미지

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "image_urls_json", columnDefinition = "json")
    private List<String> imageUrls;                   // 추가 이미지 배열

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PostStatus status = PostStatus.OPEN;      // 상태

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;                  // 작성일

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostApplication> applications = new ArrayList<>();




}
