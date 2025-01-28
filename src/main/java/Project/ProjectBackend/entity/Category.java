package Project.ProjectBackend.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "CATEGORY")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @NotEmpty
    private String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Item> items = new ArrayList<>();


    public void addChildCategory(Category child) {
        children.add(child);
        child.setParent(this);
    }

}

