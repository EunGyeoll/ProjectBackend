package Project.ProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "post_category")
public class PostCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_category_id")
    private Long categoryId;

    @Column(nullable = false, unique = true)
    private String categoryName;

    @JsonProperty("groupName")
    private String groupName;

    @OneToMany(mappedBy = "postCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @Builder
    public PostCategory(String categoryName) {
        this.categoryName = categoryName;
        this.posts = new ArrayList<>();
    }

    @Column(name = "sort_order")
    private Integer sortOrder;

}
